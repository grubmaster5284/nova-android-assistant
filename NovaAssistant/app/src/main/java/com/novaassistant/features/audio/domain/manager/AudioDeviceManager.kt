package com.novaassistant.features.audio.domain.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * Manages audio device switching and routing for both input (microphone) and output (speakers/headphones).
 *
 * Features:
 * - Monitors audio device connections/disconnections
 * - Handles Bluetooth audio devices
 * - Manages audio focus for recording
 * - Provides callbacks for device changes
 * - Optimizes device switching latency
 */
class AudioDeviceManager(private val context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var isRegistered = false
    private var deviceChangeListener: AudioDeviceChangeListener? = null
    private var audioDeviceCallback: android.media.AudioDeviceCallback? = null

    companion object {
        private const val TAG = "AudioDeviceManager"
    }

    /**
     * Listener for audio device changes
     */
    interface AudioDeviceChangeListener {
        fun onAudioDeviceConnected(device: DeviceInfo)
        fun onAudioDeviceDisconnected(device: DeviceInfo)
        fun onAudioRoutingChanged()
    }

    /**
     * Represents audio device information
     */
    data class DeviceInfo(
        val type: DeviceType,
        val name: String,
        val isInput: Boolean,
        val isOutput: Boolean
    )

    /**
     * Supported audio device types
     */
    enum class DeviceType {
        WIRED_HEADSET,
        WIRED_HEADPHONES,
        BLUETOOTH,
        SPEAKER,
        EARPIECE,
        USB,
        UNKNOWN
    }

    /**
     * Start monitoring audio device changes
     */
    fun startMonitoring(listener: AudioDeviceChangeListener) {
        if (isRegistered) {
            Log.w(TAG, "Already monitoring audio devices")
            return
        }

        this.deviceChangeListener = listener
        isRegistered = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use AudioDeviceCallback for Android M+
            registerAudioDeviceCallback()
        } else {
            // Use BroadcastReceiver for older versions
            registerBroadcastReceivers()
        }

        // Log current devices
        logCurrentDevices()

        Log.i(TAG, "Started monitoring audio devices")
    }

    /**
     * Stop monitoring audio device changes
     */
    fun stopMonitoring() {
        if (!isRegistered) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            unregisterAudioDeviceCallback()
        } else {
            unregisterBroadcastReceivers()
        }

        deviceChangeListener = null
        isRegistered = false

        Log.i(TAG, "Stopped monitoring audio devices")
    }

    /**
     * Request audio focus for recording
     */
    fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = android.media.AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }

        val granted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.d(TAG, "Audio focus requested: ${if (granted) "GRANTED" else "DENIED"}")
        return granted
    }

    /**
     * Release audio focus
     */
    fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = android.media.AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ).build()
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }

        Log.d(TAG, "Audio focus released")
    }

    /**
     * Get all connected audio input devices (microphones)
     */
    fun getConnectedInputDevices(): List<DeviceInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return emptyList()
        }

        return audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .map { device -> device.toDeviceInfo() }
            .also { devices ->
                Log.d(TAG, "Connected input devices: ${devices.size}")
                devices.forEach { Log.d(TAG, "  - ${it.name} (${it.type})") }
            }
    }

    /**
     * Get all connected audio output devices (speakers/headphones)
     */
    fun getConnectedOutputDevices(): List<DeviceInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return emptyList()
        }

        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .map { device -> device.toDeviceInfo() }
            .also { devices ->
                Log.d(TAG, "Connected output devices: ${devices.size}")
                devices.forEach { Log.d(TAG, "  - ${it.name} (${it.type})") }
            }
    }

    /**
     * Get the currently active/preferred audio device for communication
     * Returns the device that the system is currently routing audio to
     */
    fun getPreferredCommunicationDevice(): DeviceInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ has direct communication device API
            val commDevice = audioManager.communicationDevice
            return commDevice?.toDeviceInfo()?.also {
                Log.d(TAG, "Preferred communication device: ${it.name} (${it.type})")
            }
        } else {
            // For older versions, infer from system state
            return inferPreferredDevice()
        }
    }

    /**
     * Infer the preferred audio device from system state (for Android < 12)
     */
    private fun inferPreferredDevice(): DeviceInfo? {
        return when {
            audioManager.isBluetoothScoOn -> {
                DeviceInfo(DeviceType.BLUETOOTH, "Bluetooth Device", true, true)
            }
            audioManager.isWiredHeadsetOn -> {
                DeviceInfo(DeviceType.WIRED_HEADSET, "Wired Headset", true, true)
            }
            audioManager.isSpeakerphoneOn -> {
                DeviceInfo(DeviceType.SPEAKER, "Speakerphone", false, true)
            }
            else -> {
                DeviceInfo(DeviceType.EARPIECE, "Earpiece", false, true)
            }
        }.also {
            Log.d(TAG, "Inferred preferred device: ${it.name} (${it.type})")
        }
    }

    /**
     * Force update audio routing to ensure it follows the current default device
     * This should be called when device changes are detected
     */
    fun forceAudioRoutingUpdate() {
        Log.d(TAG, "=== FORCING AUDIO ROUTING UPDATE ===")

        // Get current preferred device
        val preferredDevice = getPreferredCommunicationDevice()
        Log.d(TAG, "Current preferred device: ${preferredDevice?.name} (${preferredDevice?.type})")

        // Update Bluetooth SCO state based on device
        when (preferredDevice?.type) {
            DeviceType.BLUETOOTH -> {
                if (!audioManager.isBluetoothScoOn && isBluetoothScoAvailable()) {
                    startBluetoothSco()
                }
            }
            else -> {
                if (audioManager.isBluetoothScoOn) {
                    stopBluetoothSco()
                }
            }
        }

        Log.d(TAG, "Audio mode: ${getCurrentAudioMode()}")
        Log.d(TAG, "====================================")
    }

    /**
     * Set speaker mode (enable/disable speakerphone)
     */
    fun setSpeakerMode(enabled: Boolean) {
        audioManager.isSpeakerphoneOn = enabled
        Log.d(TAG, "Speaker mode: ${if (enabled) "ON" else "OFF"}")
    }

    /**
     * Force audio mode to COMMUNICATION for better device routing
     * This makes Android treat audio like a VoIP call, which routes better
     */
    fun setAudioModeForVoice() {
        try {
            // Set mode to COMMUNICATION for VoIP-like behavior
            // This ensures audio routes to headphones/Bluetooth when connected
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            Log.d(TAG, "Audio mode set to COMMUNICATION for better device routing")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set audio mode: ${e.message}")
        }
    }

    /**
     * Reset audio mode to normal
     */
    fun resetAudioMode() {
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            Log.d(TAG, "Audio mode reset to NORMAL")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset audio mode: ${e.message}")
        }
    }

    /**
     * Get current audio mode
     */
    fun getCurrentAudioMode(): String {
        return when (audioManager.mode) {
            AudioManager.MODE_NORMAL -> "NORMAL"
            AudioManager.MODE_RINGTONE -> "RINGTONE"
            AudioManager.MODE_IN_CALL -> "IN_CALL"
            AudioManager.MODE_IN_COMMUNICATION -> "COMMUNICATION"
            else -> "UNKNOWN"
        }
    }

    /**
     * Check if Bluetooth SCO is available
     */
    fun isBluetoothScoAvailable(): Boolean {
        return audioManager.isBluetoothScoAvailableOffCall
    }

    /**
     * Start Bluetooth SCO for Bluetooth headset audio
     */
    fun startBluetoothSco() {
        if (isBluetoothScoAvailable()) {
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            Log.d(TAG, "Bluetooth SCO started")
        } else {
            Log.w(TAG, "Bluetooth SCO not available")
        }
    }

    /**
     * Stop Bluetooth SCO
     */
    fun stopBluetoothSco() {
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        Log.d(TAG, "Bluetooth SCO stopped")
    }

    /**
     * Register AudioDeviceCallback for Android M+
     */
    private fun registerAudioDeviceCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        audioDeviceCallback = object : android.media.AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                Log.i(TAG, "=== AUDIO DEVICES ADDED ===")
                addedDevices.forEach { device ->
                    val deviceInfo = device.toDeviceInfo()
                    Log.i(TAG, "Device added: ${deviceInfo.name} (${deviceInfo.type})")
                    deviceChangeListener?.onAudioDeviceConnected(deviceInfo)
                }
                deviceChangeListener?.onAudioRoutingChanged()
                logCurrentDevices()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                Log.i(TAG, "=== AUDIO DEVICES REMOVED ===")
                removedDevices.forEach { device ->
                    val deviceInfo = device.toDeviceInfo()
                    Log.i(TAG, "Device removed: ${deviceInfo.name} (${deviceInfo.type})")
                    deviceChangeListener?.onAudioDeviceDisconnected(deviceInfo)
                }
                deviceChangeListener?.onAudioRoutingChanged()
                logCurrentDevices()
            }
        }

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    /**
     * Unregister AudioDeviceCallback
     */
    private fun unregisterAudioDeviceCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        audioDeviceCallback?.let { callback ->
            audioManager.unregisterAudioDeviceCallback(callback)
        }
        audioDeviceCallback = null
    }

    /**
     * Register BroadcastReceivers for older Android versions
     */
    private fun registerBroadcastReceivers() {
        val intentFilter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }
        context.registerReceiver(audioDeviceReceiver, intentFilter)
    }

    /**
     * Unregister BroadcastReceivers
     */
    private fun unregisterBroadcastReceivers() {
        try {
            context.unregisterReceiver(audioDeviceReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Receiver not registered: ${e.message}")
        }
    }

    /**
     * BroadcastReceiver for audio device changes (pre-Android M)
     */
    private val audioDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AudioManager.ACTION_HEADSET_PLUG, Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    val name = intent.getStringExtra("name") ?: "Headset"
                    val hasMic = intent.getIntExtra("microphone", 0) == 1

                    val deviceInfo = DeviceInfo(
                        type = if (hasMic) DeviceType.WIRED_HEADSET else DeviceType.WIRED_HEADPHONES,
                        name = name,
                        isInput = hasMic,
                        isOutput = true
                    )

                    when (state) {
                        1 -> {
                            Log.i(TAG, "Headset connected: $name")
                            deviceChangeListener?.onAudioDeviceConnected(deviceInfo)
                        }
                        0 -> {
                            Log.i(TAG, "Headset disconnected: $name")
                            deviceChangeListener?.onAudioDeviceDisconnected(deviceInfo)
                        }
                    }
                    deviceChangeListener?.onAudioRoutingChanged()
                }

                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                    Log.d(TAG, "Bluetooth SCO state: $state")
                    deviceChangeListener?.onAudioRoutingChanged()
                }
            }
        }
    }

    /**
     * Convert AudioDeviceInfo to DeviceInfo
     */
    private fun AudioDeviceInfo.toDeviceInfo(): DeviceInfo {
        val deviceType = when (type) {
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> DeviceType.WIRED_HEADSET
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> DeviceType.WIRED_HEADPHONES
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> DeviceType.BLUETOOTH
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> DeviceType.BLUETOOTH
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> DeviceType.SPEAKER
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> DeviceType.EARPIECE
            AudioDeviceInfo.TYPE_USB_DEVICE -> DeviceType.USB
            AudioDeviceInfo.TYPE_USB_HEADSET -> DeviceType.USB
            else -> DeviceType.UNKNOWN
        }

        return DeviceInfo(
            type = deviceType,
            name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                productName.toString().ifEmpty { "Audio Device" }
            } else {
                "Audio Device"
            },
            isInput = isSource,
            isOutput = isSink
        )
    }

    /**
     * Log all currently connected devices
     */
    private fun logCurrentDevices() {
        Log.d(TAG, "=== CURRENT AUDIO DEVICES ===")
        Log.d(TAG, "Input devices:")
        getConnectedInputDevices()
        Log.d(TAG, "Output devices:")
        getConnectedOutputDevices()
        Log.d(TAG, "============================")
    }
}
