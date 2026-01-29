package com.ctrlbsketr.novaassistant.features.settings.domain

/**
 * Sealed interface for settings-related errors.
 * Following Clean Architecture - type-safe domain errors.
 */
sealed interface SettingsError {
    data object DataStoreNotAvailable : SettingsError
    data object ReadFailed : SettingsError
    data object WriteFailed : SettingsError
    data class Unknown(val message: String) : SettingsError
}
