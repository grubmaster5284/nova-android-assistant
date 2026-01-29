package com.ctrlbsketr.novaassistant.features.wakeword.domain.usecases

import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import javax.inject.Inject

/**
 * Use case for resuming wake word detection after pause.
 * Fast resume without reinitialization.
 *
 * @param wakeWordRepository Repository for wake word operations
 */
class ResumeWakeWordDetectionUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository
) {
    /**
     * Resume wake word listening.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return wakeWordRepository.resumeListening()
    }
}
