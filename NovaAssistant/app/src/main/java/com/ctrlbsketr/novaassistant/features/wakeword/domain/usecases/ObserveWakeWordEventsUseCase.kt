package com.ctrlbsketr.novaassistant.features.wakeword.domain.usecases

import com.ctrlbsketr.novaassistant.features.wakeword.domain.model.WakeWordEvent
import com.ctrlbsketr.novaassistant.features.settings.domain.repository.SettingsRepository
import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

/**
 * Use case for observing wake word detection events.
 * Combines wake word events with settings to filter by confidence threshold.
 *
 * Demonstrates composition of multiple data sources (Repository pattern).
 *
 * @param wakeWordRepository Repository for wake word events
 * @param settingsRepository Repository for user settings
 */
class ObserveWakeWordEventsUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Observe wake word events, filtered by user's sensitivity setting.
     *
     * @return Flow of WakeWordEvent that meet the confidence threshold
     */
    operator fun invoke(): Flow<WakeWordEvent> {
        return combine(
            wakeWordRepository.observeWakeWordEvents(),
            settingsRepository.observeSettings()
        ) { event, settings ->
            android.util.Log.d("ObserveWakeWordEvents", "=== WAKE WORD EVENT IN USE CASE ===")
            android.util.Log.d("ObserveWakeWordEvents", "Event: $event")
            android.util.Log.d("ObserveWakeWordEvents", "Sensitivity threshold: ${settings.wakeSensitivity}")
            android.util.Log.d("ObserveWakeWordEvents", "Meets threshold: ${event.meetsThreshold(settings.wakeSensitivity)}")

            // Only emit events that meet the sensitivity threshold
            val result = if (event.meetsThreshold(settings.wakeSensitivity)) {
                event
            } else {
                null
            }

            android.util.Log.d("ObserveWakeWordEvents", "Emitting: $result")
            android.util.Log.d("ObserveWakeWordEvents", "===================================")
            result
        }.filterNotNull()
    }
}
