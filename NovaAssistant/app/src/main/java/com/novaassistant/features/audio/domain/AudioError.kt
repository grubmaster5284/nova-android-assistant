package com.novaassistant.features.audio.domain

/**
 * Sealed interface for audio-related errors.
 * Following Clean Architecture - type-safe domain errors.
 */
sealed interface AudioError {
    data object AudioManagerNotAvailable : AudioError
    data object DeviceNotFound : AudioError
    data object PlaybackFailed : AudioError
    data class Unknown(val message: String) : AudioError
}
