package com.ctrlbsketr.novaassistant.util

import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.util.Log

/**
 * Utility for playing audio feedback sounds.
 * Plays a "bing" sound when wake word is detected.
 */
object SoundPlayer {
    private const val TAG = "SoundPlayer"
    private var toneGenerator: ToneGenerator? = null

    /**
     * Initialize the sound player.
     * Should be called once when the app starts.
     */
    fun initialize(context: Context) {
        try {
            // Use ToneGenerator for a simple beep sound
            // TONE_CDMA_ALERT_CALL_GUARD is a pleasant "bing" sound
            toneGenerator = ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                100 // Volume percentage (0-100)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e)
        }
    }

    /**
     * Play a "bing" sound for wake word detection.
     */
    fun playBing() {
        try {
            toneGenerator?.startTone(
                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                200 // Duration in milliseconds
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play bing sound", e)
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}

