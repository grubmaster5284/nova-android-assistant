package com.ctrlbsketr.novaassistant.features.settings.domain.usecases

import com.ctrlbsketr.novaassistant.features.settings.domain.model.Settings
import com.ctrlbsketr.novaassistant.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving user settings.
 * Provides both snapshot and observable access to settings.
 *
 * @param settingsRepository Repository for settings operations
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get current settings snapshot.
     */
    suspend fun get(): Settings {
        return settingsRepository.getSettings()
    }

    /**
     * Observe settings changes in real-time.
     */
    fun observe(): Flow<Settings> {
        return settingsRepository.observeSettings()
    }
}
