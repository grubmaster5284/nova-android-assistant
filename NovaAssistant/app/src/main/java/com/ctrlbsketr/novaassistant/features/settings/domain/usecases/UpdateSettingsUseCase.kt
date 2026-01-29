package com.ctrlbsketr.novaassistant.features.settings.domain.usecases

import com.ctrlbsketr.novaassistant.features.settings.domain.model.Settings
import com.ctrlbsketr.novaassistant.features.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating user settings.
 * Validates and persists settings changes.
 *
 * @param settingsRepository Repository for settings operations
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Update all settings.
     *
     * @param settings New settings to apply
     */
    suspend operator fun invoke(settings: Settings) {
        settingsRepository.updateSettings(settings)
    }

    /**
     * Update only service enabled state.
     */
    suspend fun setServiceEnabled(enabled: Boolean) {
        settingsRepository.setServiceEnabled(enabled)
    }
}
