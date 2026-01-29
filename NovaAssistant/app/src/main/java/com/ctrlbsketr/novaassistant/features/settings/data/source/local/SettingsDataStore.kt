package com.ctrlbsketr.novaassistant.features.settings.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ctrlbsketr.novaassistant.features.settings.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_settings")

/**
 * DataStore implementation for persisting user settings.
 * Follows Single Responsibility Principle - handles only settings persistence.
 *
 * Uses DataStore (modern replacement for SharedPreferences) for:
 * - Type safety
 * - Asynchronous operations
 * - Data consistency
 * - Kotlin Flow integration
 *
 * @param context Application context for DataStore access
 */
@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        private val KEY_WAKE_SENSITIVITY = floatPreferencesKey("wake_sensitivity")
        private val KEY_WAKE_WORD = stringPreferencesKey("wake_word")
        private val KEY_AUDIO_FEEDBACK = booleanPreferencesKey("audio_feedback")
        private val KEY_VISUAL_FEEDBACK = booleanPreferencesKey("visual_feedback")
        private val KEY_UI_IMPLEMENTATION = stringPreferencesKey("ui_implementation")
    }

    /**
     * Observe settings changes as a Flow.
     */
    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            isServiceEnabled = preferences[KEY_SERVICE_ENABLED] ?: false,
            wakeSensitivity = preferences[KEY_WAKE_SENSITIVITY] ?: 0.35f,
            wakeWord = preferences[KEY_WAKE_WORD] ?: "hey nova",
            enableAudioFeedback = preferences[KEY_AUDIO_FEEDBACK] ?: true,
            enableVisualFeedback = preferences[KEY_VISUAL_FEEDBACK] ?: true,
            uiImplementation = preferences[KEY_UI_IMPLEMENTATION] ?: "PRIMARY"
        )
    }

    /**
     * Update all settings.
     */
    suspend fun updateSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[KEY_SERVICE_ENABLED] = settings.isServiceEnabled
            preferences[KEY_WAKE_SENSITIVITY] = settings.wakeSensitivity
            preferences[KEY_WAKE_WORD] = settings.wakeWord
            preferences[KEY_AUDIO_FEEDBACK] = settings.enableAudioFeedback
            preferences[KEY_VISUAL_FEEDBACK] = settings.enableVisualFeedback
            preferences[KEY_UI_IMPLEMENTATION] = settings.uiImplementation
        }
    }

    /**
     * Update service enabled state.
     */
    suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SERVICE_ENABLED] = enabled
        }
    }

    /**
     * Update wake sensitivity.
     */
    suspend fun setWakeSensitivity(sensitivity: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_WAKE_SENSITIVITY] = sensitivity
        }
    }

    /**
     * Update UI implementation version.
     */
    suspend fun setUiImplementation(implementation: String) {
        dataStore.edit { preferences ->
            preferences[KEY_UI_IMPLEMENTATION] = implementation
        }
    }

    /**
     * Reset all settings to defaults.
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
