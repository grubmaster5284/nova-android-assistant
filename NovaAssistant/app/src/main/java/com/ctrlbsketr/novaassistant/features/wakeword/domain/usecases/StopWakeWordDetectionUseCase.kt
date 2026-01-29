package com.ctrlbsketr.novaassistant.features.wakeword.domain.usecases

import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import javax.inject.Inject

/**
 * Use case for stopping wake word detection.
 * Follows Single Responsibility Principle - handles only stop logic.
 *
 * @param wakeWordRepository Repository for wake word operations
 */
class StopWakeWordDetectionUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository
) {
    /**
     * Execute the use case to stop wake word detection.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Check if service is running before attempting to stop
            if (!wakeWordRepository.isServiceRunning()) {
                return Result.success(Unit)
            }

            // Stop the service
            wakeWordRepository.stopWakeWordDetection()
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Failed to stop wake word detection: ${e.message}", e)
            )
        }
    }
}
