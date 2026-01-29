package com.ctrlbsketr.novaassistant.features.wakeword.domain.usecases

import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import javax.inject.Inject

/**
 * Use case for pausing wake word detection.
 * Service stays running but stops processing audio - avoids reinitialization overhead.
 *
 * @param wakeWordRepository Repository for wake word operations
 */
class PauseWakeWordDetectionUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository
) {
    /**
     * Pause wake word listening.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return wakeWordRepository.pauseListening()
    }
}
