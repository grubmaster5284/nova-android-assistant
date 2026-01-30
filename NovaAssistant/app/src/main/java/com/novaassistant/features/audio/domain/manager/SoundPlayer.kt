package com.novaassistant.features.audio.domain.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.util.Log

/**
 * Utility for playing audio feedback sounds.
 * Plays a "bing" sound when wake word is detected.
 *
 * Updated to support faster audio device switching with proper audio attributes
 * and routing to the correct output device (speakers, headphones, Bluetooth, etc.)
 */
object SoundPlayer {
    private const val TAG = "SoundPlayer"
    private var toneGenerator: ToneGenerator? = null
    private var audioManager: AudioManager? = null

    /**
     * Initialize the sound player.
     * Should be called once when the app starts.
     */
    fun initialize(context: Context) {
        try {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Use STREAM_VOICE_CALL for VoIP-like routing
            // This ensures audio feedback goes to the same device as voice input
            // (headphones, Bluetooth, speaker, etc.)
            val streamType = AudioManager.STREAM_VOICE_CALL

            toneGenerator = ToneGenerator(
                streamType,
                80 // Volume percentage (0-100) - slightly reduced for better UX
            )

            Log.d(TAG, "SoundPlayer initialized with STREAM_VOICE_CALL for matched device routing")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e)
        }
    }

    /**
     * Play a "bing" sound for wake word detection.
     * Automatically routes to the current audio output device.
     */
    fun playBing() {
        try {
            // Log current audio routing for debugging
            logCurrentAudioRoute()

            // Play tone - ToneGenerator automatically uses system audio routing
            // which follows the current output device (speaker, headphones, Bluetooth, etc.)
            toneGenerator?.startTone(
                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                200 // Duration in milliseconds
            )

            Log.d(TAG, "Bing sound played successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play bing sound", e)
        }
    }

    /**
     * Log current audio routing for debugging
     */
    private fun logCurrentAudioRoute() {
        audioManager?.let { am ->
            val routing = buildString {
                append("Audio routing: ")
                when {
                    am.isBluetoothScoOn -> append("Bluetooth SCO")
                    am.isBluetoothA2dpOn -> append("Bluetooth A2DP")
                    am.isSpeakerphoneOn -> append("Speakerphone")
                    am.isWiredHeadsetOn -> append("Wired headset")
                    else -> append("Default (speaker/earpiece)")
                }
            }
            Log.d(TAG, routing)
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        audioManager = null
        Log.d(TAG, "SoundPlayer released")
    }
}

