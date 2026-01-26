package com.ctrlbsketr.novaassistant.domain.usecases

import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.domain.repository.SettingsRepository
import com.ctrlbsketr.novaassistant.domain.repository.WakeWordRepository
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
            // Only emit events that meet the sensitivity threshold
            if (event.meetsThreshold(settings.wakeSensitivity)) {
                event
            } else {
                null
            }
        }.filterNotNull()
    }
}
