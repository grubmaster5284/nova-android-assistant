package com.ctrlbsketr.novaassistant.data.repository

import android.content.Context
import com.ctrlbsketr.novaassistant.data.service.WakeWordService
import com.ctrlbsketr.novaassistant.domain.models.ServiceStatus
import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.domain.repository.WakeWordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WakeWordRepository.
 * Follows Dependency Inversion Principle - depends on abstractions.
 *
 * Acts as a bridge between domain layer and Android service.
 * Provides abstraction over service lifecycle and event observation.
 *
 * This implementation demonstrates:
 * - Liskov Substitution Principle: Can be swapped with mock implementation
 * - Interface Segregation Principle: Clean, focused interface
 * - Single Responsibility: Manages only wake word service interactions
 *
 * @param context Application context for service operations
 */
@Singleton
class WakeWordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WakeWordRepository {

    override suspend fun startWakeWordDetection(): Result<Unit> {
        return try {
            WakeWordService.start(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Failed to start wake word service: ${e.message}", e)
            )
        }
    }

    override suspend fun stopWakeWordDetection(): Result<Unit> {
        return try {
            WakeWordService.stop(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Failed to stop wake word service: ${e.message}", e)
            )
        }
    }

    override fun observeWakeWordEvents(): Flow<WakeWordEvent> {
        return WakeWordService.wakeWordEvents
            .filterNotNull() // Only emit non-null events
    }

    override fun observeServiceStatus(): Flow<ServiceStatus> {
        return WakeWordService.serviceStatus
    }

    override suspend fun isServiceRunning(): Boolean {
        return WakeWordService.serviceStatus.first().isRunning
    }

    override suspend fun getServiceStatus(): ServiceStatus {
        return WakeWordService.serviceStatus.first()
    }
}
