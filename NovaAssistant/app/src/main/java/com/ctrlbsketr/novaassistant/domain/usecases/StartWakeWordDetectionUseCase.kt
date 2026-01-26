package com.ctrlbsketr.novaassistant.domain.usecases

import com.ctrlbsketr.novaassistant.domain.repository.WakeWordRepository
import javax.inject.Inject

/**
 * Use case for starting wake word detection.
 * Follows Single Responsibility Principle - handles only start logic.
 *
 * Encapsulates business logic for:
 * - Starting the wake word service
 * - Error handling
 * - Validation
 *
 * @param wakeWordRepository Repository for wake word operations
 */
class StartWakeWordDetectionUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository
) {
    /**
     * Execute the use case to start wake word detection.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Check if already running to avoid duplicate starts
            if (wakeWordRepository.isServiceRunning()) {
                return Result.success(Unit)
            }

            // Start the service
            wakeWordRepository.startWakeWordDetection()
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Failed to start wake word detection: ${e.message}", e)
            )
        }
    }
}
