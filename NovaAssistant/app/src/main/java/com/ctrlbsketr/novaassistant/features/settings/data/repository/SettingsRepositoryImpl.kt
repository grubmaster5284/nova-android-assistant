package com.ctrlbsketr.novaassistant.features.settings.data.repository

import com.ctrlbsketr.novaassistant.features.settings.data.source.local.SettingsDataStore
import com.ctrlbsketr.novaassistant.features.settings.domain.model.Settings
import com.ctrlbsketr.novaassistant.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository.
 * Follows Dependency Inversion Principle - depends on abstraction (SettingsDataStore).
 *
 * Acts as a bridge between domain layer and data source.
 * Enables easy swapping of data sources without affecting domain layer.
 *
 * @param dataStore Local data store for settings persistence
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun observeSettings(): Flow<Settings> {
        return dataStore.settingsFlow
    }

    override suspend fun getSettings(): Settings {
        return dataStore.settingsFlow.first()
    }

    override suspend fun updateSettings(settings: Settings) {
        dataStore.updateSettings(settings)
    }

    override suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.setServiceEnabled(enabled)
    }

    override suspend fun setWakeSensitivity(sensitivity: Float) {
        require(sensitivity in 0.0f..1.0f) {
            "Sensitivity must be between 0.0 and 1.0"
        }
        dataStore.setWakeSensitivity(sensitivity)
    }

    override suspend fun resetToDefaults() {
        dataStore.clear()
    }
}
