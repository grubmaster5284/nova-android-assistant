package com.ctrlbsketr.novaassistant.domain.model

/**
 * Represents the current state of the voice assistant.
 * Used to drive UI animations and status messages.
 */
sealed class AssistantState {
    /**
     * Idle state - waiting for wake word detection
     */
    object Idle : AssistantState()

    /**
     * Wake word has been detected
     */
    object WakeDetected : AssistantState()

    /**
     * User is speaking their command
     * @param audioAmplitude Current audio input amplitude (0.0 to 1.0) for ripple animation
     */
    data class UserSpeaking(val audioAmplitude: Float = 0f) : AssistantState()

    /**
     * Processing user's command (thinking/API call)
     */
    object Processing : AssistantState()

    /**
     * Assistant is speaking response via TTS
     * @param text The text being spoken
     * @param audioAmplitude Current TTS audio amplitude (0.0 to 1.0) for glow animation
     */
    data class AssistantSpeaking(
        val text: String,
        val audioAmplitude: Float = 0f
    ) : AssistantState()

    /**
     * Error state with specific error message
     * @param message Human-readable error description
     */
    data class Error(val message: String) : AssistantState()
}

/**
 * Status messages that correspond to each assistant state.
 * Used for typewriter text animation below the orb.
 */
sealed class StatusMessage(val text: String) {
    object Idle : StatusMessage("Listening for wake word...")
    object WakeDetected : StatusMessage("Wake word detected!")
    object UserSpeaking : StatusMessage("Listening...")
    object Processing : StatusMessage("Thinking...")
    object AssistantSpeaking : StatusMessage("Speaking...")
    data class Error(val errorMsg: String) : StatusMessage("Error: $errorMsg")

    companion object {
        /**
         * Get the appropriate status message for a given assistant state
         */
        fun fromAssistantState(state: AssistantState): StatusMessage {
            return when (state) {
                is AssistantState.Idle -> Idle
                is AssistantState.WakeDetected -> WakeDetected
                is AssistantState.UserSpeaking -> UserSpeaking
                is AssistantState.Processing -> Processing
                is AssistantState.AssistantSpeaking -> AssistantSpeaking
                is AssistantState.Error -> Error(state.message)
            }
        }
    }
}
