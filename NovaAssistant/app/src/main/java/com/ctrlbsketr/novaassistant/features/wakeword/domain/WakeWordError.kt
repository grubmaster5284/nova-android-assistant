package com.ctrlbsketr.novaassistant.features.wakeword.domain

/**
 * Sealed interface for wake word detection errors.
 * Following Clean Architecture - type-safe domain errors.
 */
sealed interface WakeWordError {
    data object ServiceNotAvailable : WakeWordError
    data object PermissionDenied : WakeWordError
    data object PorcupineInitFailed : WakeWordError
    data object PorcupineActivationFailed : WakeWordError
    data object AudioRecordingFailed : WakeWordError
    data class Unknown(val message: String) : WakeWordError
}
