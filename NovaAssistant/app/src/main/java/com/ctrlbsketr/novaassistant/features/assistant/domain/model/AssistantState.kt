package com.ctrlbsketr.novaassistant.features.assistant.domain.model

/**
 * Represents the current state of the Nova Assistant.
 * Sealed class ensures type-safe state management (Open/Closed Principle).
 */
sealed class AssistantState {
    /**
     * Service is not running.
     */
    data object Idle : AssistantState()

    /**
     * Service is starting up and initializing.
     */
    data object Initializing : AssistantState()

    /**
     * Service is actively listening for wake word.
     */
    data object Listening : AssistantState()

    /**
     * Wake word detected, processing command.
     */
    data object Processing : AssistantState()

    /**
     * Error occurred during operation.
     * @param message Error description
     * @param throwable Optional exception
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : AssistantState()

    /**
     * Service is stopping.
     */
    data object Stopping : AssistantState()
}
