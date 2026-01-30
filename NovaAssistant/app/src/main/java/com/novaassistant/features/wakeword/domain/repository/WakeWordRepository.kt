package com.novaassistant.features.wakeword.domain.repository

import com.novaassistant.features.wakeword.domain.model.ServiceStatus
import com.novaassistant.features.wakeword.domain.model.WakeWordEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for wake word detection operations.
 * Follows Interface Segregation Principle - focused contract for wake word functionality.
 *
 * Domain layer defines the contract, data layer implements it.
 * This enables:
 * - Easy testing with mock implementations
 * - Swapping implementations (e.g., Porcupine -> different engine)
 * - Clean dependency flow (domain ← data)
 */
interface WakeWordRepository {

    /**
     * Start wake word detection service.
     * @return Result indicating success or failure with error message
     */
    suspend fun startWakeWordDetection(): Result<Unit>

    /**
     * Stop wake word detection service.
     * @return Result indicating success or failure with error message
     */
    suspend fun stopWakeWordDetection(): Result<Unit>

    /**
     * Observe wake word detection events as they occur.
     * @return Flow emitting WakeWordEvent when wake word is detected
     */
    fun observeWakeWordEvents(): Flow<WakeWordEvent>

    /**
     * Observe service status changes.
     * @return Flow emitting current service status
     */
    fun observeServiceStatus(): Flow<ServiceStatus>

    /**
     * Check if wake word service is currently running.
     * @return true if service is active, false otherwise
     */
    suspend fun isServiceRunning(): Boolean

    /**
     * Get current service status snapshot.
     * @return Current ServiceStatus
     */
    suspend fun getServiceStatus(): ServiceStatus

    /**
     * Pause wake word listening (service stays running, stops processing audio).
     * Useful for temporary muting without service restart overhead.
     * @return Result indicating success or failure
     */
    suspend fun pauseListening(): Result<Unit>

    /**
     * Resume wake word listening after pause.
     * @return Result indicating success or failure
     */
    suspend fun resumeListening(): Result<Unit>
}
