package com.novaassistant.features.settings.domain.repository

import com.novaassistant.features.settings.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user settings operations.
 * Follows Single Responsibility Principle - handles only settings persistence.
 *
 * Abstracts the underlying storage mechanism (DataStore, SharedPreferences, etc.)
 * allowing easy swapping of implementations.
 */
interface SettingsRepository {

    /**
     * Observe settings changes in real-time.
     * @return Flow emitting current settings whenever they change
     */
    fun observeSettings(): Flow<Settings>

    /**
     * Get current settings snapshot.
     * @return Current Settings object
     */
    suspend fun getSettings(): Settings

    /**
     * Update user settings.
     * @param settings New settings to persist
     */
    suspend fun updateSettings(settings: Settings)

    /**
     * Update service enabled state.
     * Convenience method for toggling service on/off.
     */
    suspend fun setServiceEnabled(enabled: Boolean)

    /**
     * Update wake word sensitivity.
     * @param sensitivity New sensitivity value (0.0 to 1.0)
     */
    suspend fun setWakeSensitivity(sensitivity: Float)

    /**
     * Reset settings to default values.
     */
    suspend fun resetToDefaults()
}
