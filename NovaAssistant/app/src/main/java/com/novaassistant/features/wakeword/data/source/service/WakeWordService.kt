package com.novaassistant.features.wakeword.data.source.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.novaassistant.BuildConfig
import com.novaassistant.MainActivity
import com.novaassistant.R
import com.novaassistant.features.wakeword.domain.model.ServiceStatus
import com.novaassistant.features.wakeword.domain.model.WakeWordEvent
import com.novaassistant.features.settings.domain.repository.SettingsRepository
import ai.picovoice.porcupine.PorcupineActivationException
import ai.picovoice.porcupine.PorcupineActivationLimitException
import ai.picovoice.porcupine.PorcupineActivationRefusedException
import ai.picovoice.porcupine.PorcupineActivationThrottledException
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineInvalidArgumentException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Foreground service for wake word detection using Porcupine.
 * Follows the official Porcupine Android Service demo implementation pattern.
 *
 * Implementation based on: https://github.com/Picovoice/porcupine/tree/master/demo/android/Service
 *
 * Key responsibilities:
 * - Maintain foreground service lifecycle
 * - Initialize and manage Porcupine wake word engine
 * - Emit service status and wake word events
 * - Handle service start/stop requests
 *
 * @see <a href="https://github.com/Picovoice/porcupine">Porcupine GitHub</a>
 */
@AndroidEntryPoint
class WakeWordService : Service() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var porcupineManager: PorcupineManager? = null
    private var detectionCount: Int = 0
    private var serviceStartTime: Long? = null  // Track when service was started
    private var audioDeviceManager: com.novaassistant.features.audio.domain.manager.AudioDeviceManager? = null

    companion object {
        // Custom wake word file path in assets
        private const val CUSTOM_WAKE_WORD_PATH = "Hey-Nova_en_android_v4_0_0.ppn"
        
        private const val CHANNEL_ID = "nova_wake_word_channel"
        private const val CHANNEL_NAME = "Nova Wake Word Detection"
        private const val NOTIFICATION_ID = 1001

        private const val ACTION_START = "com.novaassistant.START_WAKE_WORD"
        private const val ACTION_STOP = "com.novaassistant.STOP_WAKE_WORD"
        private const val ACTION_PAUSE = "com.novaassistant.PAUSE_WAKE_WORD"
        private const val ACTION_RESUME = "com.novaassistant.RESUME_WAKE_WORD"

        const val BROADCAST_PORCUPINE_ERROR = "com.novaassistant.PORCUPINE_ERROR"
        const val EXTRA_ERROR_MESSAGE = "errorMessage"

        // Shared state flows for observing service status and events
        private val _serviceStatus = MutableStateFlow(ServiceStatus())
        val serviceStatus: StateFlow<ServiceStatus> = _serviceStatus.asStateFlow()

        private val _wakeWordEvents = MutableStateFlow<WakeWordEvent?>(null)
        val wakeWordEvents: StateFlow<WakeWordEvent?> = _wakeWordEvents.asStateFlow()

        /**
         * Start wake word detection service.
         */
        fun start(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop wake word detection service.
         */
        fun stop(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        /**
         * Pause wake word listening (service stays running).
         */
        fun pause(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        /**
         * Resume wake word listening.
         */
        fun resume(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }
    }

    /**
     * Porcupine callback - invoked when wake word is detected.
     * Following official demo pattern.
     */
    private val porcupineManagerCallback = PorcupineManagerCallback { keywordIndex ->
        detectionCount++

        // Create wake word event
        val event = WakeWordEvent(
            keyword = getKeywordName(keywordIndex),
            confidence = 1.0f, // Porcupine doesn't provide confidence
            timestamp = System.currentTimeMillis()
        )

        // Emit event to observers
        _wakeWordEvents.value = event

        // Log wake word detection to logcat
        val keywordName = getKeywordName(keywordIndex)
        android.util.Log.i("WakeWordService", "=== WAKE WORD DETECTED ===")
        android.util.Log.i("WakeWordService", "Keyword: $keywordName")
        android.util.Log.i("WakeWordService", "Detection count: $detectionCount")
        android.util.Log.i("WakeWordService", "Timestamp: ${event.timestamp}")
        android.util.Log.i("WakeWordService", "Confidence: ${event.confidence}")
        android.util.Log.i("WakeWordService", "========================")
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Initialize audio device manager
        audioDeviceManager = com.novaassistant.features.audio.domain.manager.AudioDeviceManager(applicationContext)
        audioDeviceManager?.startMonitoring(object : com.novaassistant.features.audio.domain.manager.AudioDeviceManager.AudioDeviceChangeListener {
            override fun onAudioDeviceConnected(device: com.novaassistant.features.audio.domain.manager.AudioDeviceManager.DeviceInfo) {
                android.util.Log.i("WakeWordService", "Audio device connected: ${device.name} (${device.type})")
                handleAudioDeviceChange()
            }

            override fun onAudioDeviceDisconnected(device: com.novaassistant.features.audio.domain.manager.AudioDeviceManager.DeviceInfo) {
                android.util.Log.i("WakeWordService", "Audio device disconnected: ${device.name} (${device.type})")
                handleAudioDeviceChange()
            }

            override fun onAudioRoutingChanged() {
                android.util.Log.i("WakeWordService", "Audio routing changed")
                handleAudioDeviceChange()
            }
        })

        android.util.Log.i("WakeWordService", "AudioDeviceManager initialized")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWakeWordDetection()
            ACTION_STOP -> stopWakeWordDetection()
            ACTION_PAUSE -> pauseWakeWordDetection()
            ACTION_RESUME -> resumeWakeWordDetection()
        }

        return START_STICKY // Service restarts if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service doesn't support binding
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()

        // Clean up audio device manager
        audioDeviceManager?.stopBluetoothSco()
        audioDeviceManager?.releaseAudioFocus()
        audioDeviceManager?.stopMonitoring()
        audioDeviceManager = null
    }

    /**
     * Start wake word detection.
     * Follows official Porcupine demo initialization pattern.
     */
    private fun startWakeWordDetection() {
        // Check microphone permission
        if (!hasMicrophonePermission()) {
            onPorcupineInitError("Microphone permission not granted")
            return
        }

        // Validate access key before attempting initialization
        val accessKey = BuildConfig.PORCUPINE_ACCESS_KEY
        if (accessKey.isBlank()) {
            onPorcupineInitError(
                "Porcupine access key is not configured. " +
                "Please set PORCUPINE_ACCESS_KEY in your .env file. " +
                "Get a free key from: https://console.picovoice.ai/"
            )
            return
        }

        detectionCount = 0

        // Get current settings for sensitivity
        val settings = runBlocking {
            settingsRepository.getSettings()
        }

        try {
            // Set audio mode to COMMUNICATION for better device routing
            // This makes Android treat our audio like a VoIP call
            audioDeviceManager?.setAudioModeForVoice()
            android.util.Log.i("WakeWordService", "Audio mode: ${audioDeviceManager?.getCurrentAudioMode()}")

            // Request audio focus before starting
            val audioFocusGranted = audioDeviceManager?.requestAudioFocus() ?: false
            if (!audioFocusGranted) {
                android.util.Log.w("WakeWordService", "Audio focus not granted, continuing anyway")
            }

            // Force audio routing update to ensure we use the current system default
            audioDeviceManager?.forceAudioRoutingUpdate()

            // Initialize PorcupineManager with custom wake word
            // Following official demo pattern - synchronous initialization
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeywordPath(CUSTOM_WAKE_WORD_PATH) // Custom "Hey Nova" keyword from assets
                .setSensitivity(settings.wakeSensitivity) // Use sensitivity from settings
                .build(applicationContext, porcupineManagerCallback)

            // Start processing audio
            porcupineManager?.start()

            // Log the preferred audio device
            val preferredDevice = audioDeviceManager?.getPreferredCommunicationDevice()
            android.util.Log.i("WakeWordService", "Using audio device: ${preferredDevice?.name} (${preferredDevice?.type})")

            // Update service status
            serviceStartTime = System.currentTimeMillis()
            updateServiceStatus(
                isRunning = true,
                isListening = true,
                startTime = serviceStartTime
            )

            // Log successful initialization
            android.util.Log.i("WakeWordService", "=== SERVICE STARTED ===")
            android.util.Log.i("WakeWordService", "Porcupine initialized successfully")
            android.util.Log.i("WakeWordService", "Wake sensitivity: ${settings.wakeSensitivity}")
            android.util.Log.i("WakeWordService", "Start time: $serviceStartTime")
            android.util.Log.i("WakeWordService", "Status: Listening for 'Hey Nova'")
            android.util.Log.i("WakeWordService", "======================")

            // Start foreground with minimal notification
            val notification = createNotification(
                "Nova is listening",
                "Listening for 'Hey Nova'"
            )
            startForeground(NOTIFICATION_ID, notification)

        } catch (e: PorcupineInvalidArgumentException) {
            android.util.Log.e("WakeWordService", "PorcupineInvalidArgumentException", e)
            onPorcupineInitError("Invalid argument: ${e.message}\nCheck your keyword and sensitivity settings.")
        } catch (e: PorcupineActivationException) {
            android.util.Log.e("WakeWordService", "PorcupineActivationException", e)
            val errorMsg = buildString {
                append("Activation failed. ")
                append("This usually means:\n")
                append("1. Invalid or expired access key\n")
                append("2. No internet connection (required for first activation)\n")
                append("3. Access key format is incorrect\n\n")
                append("Error details: ${e.message}\n")
                if (e.cause != null) {
                    append("Cause: ${e.cause?.message}")
                }
            }
            onPorcupineInitError(errorMsg)
        } catch (e: PorcupineActivationLimitException) {
            android.util.Log.e("WakeWordService", "PorcupineActivationLimitException", e)
            onPorcupineInitError(
                "Activation limit reached. " +
                "Your access key has reached its device limit. " +
                "Check your account at https://console.picovoice.ai/"
            )
        } catch (e: PorcupineActivationRefusedException) {
            android.util.Log.e("WakeWordService", "PorcupineActivationRefusedException", e)
            onPorcupineInitError(
                "Activation refused. " +
                "Your access key was refused. " +
                "Please verify your key at https://console.picovoice.ai/ " +
                "and ensure it's valid and not expired."
            )
        } catch (e: PorcupineActivationThrottledException) {
            android.util.Log.e("WakeWordService", "PorcupineActivationThrottledException", e)
            onPorcupineInitError(
                "Activation throttled. " +
                "Too many activation requests. " +
                "Please wait a few minutes and try again."
            )
        } catch (e: PorcupineException) {
            android.util.Log.e("WakeWordService", "PorcupineException", e)
            onPorcupineInitError("Porcupine error: ${e.message}\n${e.stackTraceToString()}")
        } catch (e: Exception) {
            android.util.Log.e("WakeWordService", "Unexpected exception during Porcupine initialization", e)
            onPorcupineInitError("Unexpected error: ${e.message}\n${e.stackTraceToString()}")
        }

        // If initialization failed, start foreground with error notification
        if (porcupineManager == null) {
            val notification = createNotification(
                "Porcupine init failed",
                "Service will stop"
            )
            startForeground(NOTIFICATION_ID, notification)

            // Stop the service after a delay
            stopSelf()
        }
    }

    /**
     * Stop wake word detection and clean up resources.
     */
    private fun stopWakeWordDetection() {
        android.util.Log.i("WakeWordService", "=== SERVICE STOPPING ===")
        android.util.Log.i("WakeWordService", "Total detections: $detectionCount")
        android.util.Log.i("WakeWordService", "Cleaning up resources...")

        cleanup()

        // Stop Bluetooth SCO if active
        audioDeviceManager?.stopBluetoothSco()

        // Release audio focus
        audioDeviceManager?.releaseAudioFocus()

        // Reset audio mode to normal
        audioDeviceManager?.resetAudioMode()

        // Stop audio device monitoring
        audioDeviceManager?.stopMonitoring()

        // Update service status
        updateServiceStatus(
            isRunning = false,
            isListening = false,
            startTime = null
        )

        android.util.Log.i("WakeWordService", "Service stopped successfully")
        android.util.Log.i("WakeWordService", "=======================")

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Pause wake word detection (service stays running, stops processing audio).
     * Lightweight alternative to stopping service - avoids reinitialization overhead.
     */
    private fun pauseWakeWordDetection() {
        try {
            android.util.Log.i("WakeWordService", "=== PAUSING LISTENING ===")

            // Stop Porcupine audio processing but keep instance alive
            porcupineManager?.stop()

            // Update service status to reflect paused state
            updateServiceStatus(
                isRunning = true,  // Service still running
                isListening = false, // Not actively listening
                startTime = serviceStartTime // Preserve original start time
            )

            android.util.Log.i("WakeWordService", "Listening paused (service still running)")
            android.util.Log.i("WakeWordService", "=========================")

            // Update notification to show paused state
            val notification = createNotification(
                "Nova paused",
                "Tap to resume listening"
            )
            startForeground(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            android.util.Log.e("WakeWordService", "Error pausing detection", e)
        }
    }

    /**
     * Resume wake word detection after pause.
     * Fast resume without reinitialization.
     */
    private fun resumeWakeWordDetection() {
        try {
            android.util.Log.i("WakeWordService", "=== RESUMING LISTENING ===")

            // Resume Porcupine audio processing
            porcupineManager?.start()

            // Update service status
            updateServiceStatus(
                isRunning = true,
                isListening = true,
                startTime = serviceStartTime // Preserve original start time
            )

            android.util.Log.i("WakeWordService", "Listening resumed")
            android.util.Log.i("WakeWordService", "==========================")

            // Update notification to show active state
            val notification = createNotification(
                "Nova is listening",
                "Listening for 'Hey Nova'"
            )
            startForeground(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            android.util.Log.e("WakeWordService", "Error resuming detection", e)
            // If resume fails, try reinitializing
            android.util.Log.w("WakeWordService", "Attempting to reinitialize...")
            stopWakeWordDetection()
            startWakeWordDetection()
        }
    }

    /**
     * Clean up Porcupine resources.
     * Following official demo cleanup pattern.
     *
     * Note: We need to ensure stop() completes fully before delete() to avoid
     * race conditions where in-flight audio frames try to process on a deleted instance.
     * The frame listener in PorcupineManager may still receive frames after stop() is called
     * but before delete() completes, causing PorcupineInvalidStateException.
     */
    private fun cleanup() {
        val manager = porcupineManager
        porcupineManager = null // Set to null first to prevent new operations
        
        manager?.let {
            // Use runBlocking for synchronous cleanup in onDestroy
            // This ensures cleanup completes before service destruction
            runBlocking {
                try {
                    // Stop listening first - this removes frame listeners from VoiceProcessor
                    it.stop()
                    
                    // Small delay to allow any in-flight frames to complete processing
                    // This prevents PorcupineInvalidStateException when frames arrive
                    // after the listener is removed but before delete() completes
                    delay(100) // 100ms should be enough for any queued frames
                    
                    // Now safe to delete
                    it.delete()
                } catch (e: PorcupineException) {
                    android.util.Log.e("WakeWordService", "Error cleaning up Porcupine: ${e.message}")
                }
            }
        }
    }

    /**
     * Handle Porcupine initialization error.
     * Following official demo pattern - broadcast error to MainActivity.
     */
    private fun onPorcupineInitError(message: String) {
        android.util.Log.e("WakeWordService", "=== INITIALIZATION ERROR ===")
        android.util.Log.e("WakeWordService", "Error message: $message")
        android.util.Log.e("WakeWordService", "===========================")

        val intent = Intent(BROADCAST_PORCUPINE_ERROR).apply {
            putExtra(EXTRA_ERROR_MESSAGE, message)
            setPackage(applicationContext.packageName) // Make intent explicit for security
        }
        sendBroadcast(intent)

        updateServiceStatus(
            isRunning = false,
            isListening = false,
            startTime = null
        )
    }

    /**
     * Create notification channel for Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // High importance for wake word detection
            ).apply {
                description = "Shows when Nova is listening for wake words"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create foreground service notification.
     * Following official demo notification pattern.
     */
    private fun createNotification(title: String, message: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Add custom icon
            .setContentIntent(pendingIntent)
            .setOngoing(false) // Allow user to dismiss notification
            .build()
    }

    /**
     * Check if RECORD_AUDIO permission is granted.
     */
    private fun hasMicrophonePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Update service status state flow.
     */
    private fun updateServiceStatus(
        isRunning: Boolean,
        isListening: Boolean,
        startTime: Long?
    ) {
        _serviceStatus.value = ServiceStatus(
            isRunning = isRunning,
            isListening = isListening,
            batteryImpact = 0f, // TODO: Measure actual battery impact in Week 2
            detectionCount = detectionCount,
            startTime = startTime ?: 0L
        )
    }

    /**
     * Get keyword name from index.
     * Using custom "Hey Nova" keyword.
     */
    private fun getKeywordName(keywordIndex: Int): String {
        return when (keywordIndex) {
            0 -> "Hey Nova" // Custom keyword
            else -> "Unknown"
        }
    }

    /**
     * Handle audio device changes (headphones plugged in/out, Bluetooth connected, etc.)
     * This optimizes audio routing for the new device and forces routing updates.
     */
    private fun handleAudioDeviceChange() {
        // If service is not running, no action needed
        if (porcupineManager == null) {
            return
        }

        // Force audio routing update to switch to the new default device
        audioDeviceManager?.forceAudioRoutingUpdate()

        // Log current audio devices for debugging
        audioDeviceManager?.getConnectedInputDevices()
        audioDeviceManager?.getConnectedOutputDevices()

        // Log the new preferred device
        val preferredDevice = audioDeviceManager?.getPreferredCommunicationDevice()
        android.util.Log.i("WakeWordService", "Switched to audio device: ${preferredDevice?.name} (${preferredDevice?.type})")
        android.util.Log.i("WakeWordService", "Audio mode: ${audioDeviceManager?.getCurrentAudioMode()}")

        // Note: With COMMUNICATION mode, AudioRecord automatically follows system routing
        // Android will route audio to the preferred communication device (headphones, Bluetooth, etc.)
        // No need to restart PorcupineManager - routing happens at the system level
    }
}
