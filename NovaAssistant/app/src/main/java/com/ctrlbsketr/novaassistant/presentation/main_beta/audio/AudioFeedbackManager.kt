package com.ctrlbsketr.novaassistant.presentation.main_beta.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.ToneGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages audio feedback for the beta UI.
 * Plays beep sounds for wake word detection and errors.
 *
 * Uses ToneGenerator for programmatic tone generation.
 */
@Singleton
class AudioFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var toneGenerator: ToneGenerator? = null

    init {
        initializeToneGenerator()
    }

    private fun initializeToneGenerator() {
        try {
            toneGenerator = ToneGenerator(
                AudioAttributes.CONTENT_TYPE_SONIFICATION,
                ToneGenerator.MAX_VOLUME / 2 // 50% volume
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Play wake word detected beep.
     * Pleasant 1000Hz tone for 250ms.
     */
    fun playWakeWordBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
        } catch (e: Exception) {
            e.printStackTrace()
            // Reinitialize if failed
            initializeToneGenerator()
        }
    }

    /**
     * Play error beep.
     * Lower 500Hz tone for 400ms.
     */
    fun playErrorBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 400)
        } catch (e: Exception) {
            e.printStackTrace()
            initializeToneGenerator()
        }
    }

    /**
     * Play button press click.
     * Short 800Hz tone for 50ms.
     */
    fun playButtonClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            e.printStackTrace()
            initializeToneGenerator()
        }
    }

    /**
     * Stop all tones and release resources.
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
