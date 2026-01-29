package com.ctrlbsketr.novaassistant.features.settings.domain.model

/**
 * User settings for Nova Assistant.
 * Immutable configuration object.
 *
 * @param isServiceEnabled Whether wake word service should auto-start
 * @param wakeSensitivity Wake word detection sensitivity (0.0 to 1.0)
 * @param wakeWord The wake word phrase to listen for
 * @param enableAudioFeedback Whether to play audio on wake word detection
 * @param enableVisualFeedback Whether to show visual indicator on wake word
 * @param uiImplementation The UI implementation version to use (e.g., "PRIMARY", "V2")
 */
data class Settings(
    val isServiceEnabled: Boolean = false,
    val wakeSensitivity: Float = 0.35f,  // More sensitive - better for voice variations
    val wakeWord: String = "hey nova",
    val enableAudioFeedback: Boolean = true,
    val enableVisualFeedback: Boolean = true,
    val uiImplementation: String = "PRIMARY"
) {
    init {
        require(wakeSensitivity in 0.0f..1.0f) {
            "Wake sensitivity must be between 0.0 and 1.0"
        }
        require(wakeWord.isNotBlank()) {
            "Wake word cannot be blank"
        }
    }

    companion object {
        /**
         * Default settings for first-time users.
         */
        fun default() = Settings()

        /**
         * High sensitivity settings for quiet environments.
         */
        fun highSensitivity() = Settings(
            wakeSensitivity = 0.3f
        )

        /**
         * Low sensitivity settings for noisy environments.
         */
        fun lowSensitivity() = Settings(
            wakeSensitivity = 0.7f
        )
    }
}
