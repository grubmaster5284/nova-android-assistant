package com.ctrlbsketr.novaassistant.features.wakeword.domain.usecases

import com.ctrlbsketr.novaassistant.features.wakeword.domain.model.ServiceStatus
import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing wake word service status changes.
 * Provides real-time updates of service operational state.
 *
 * @param wakeWordRepository Repository for service status
 */
class ObserveServiceStatusUseCase @Inject constructor(
    private val wakeWordRepository: WakeWordRepository
) {
    /**
     * Observe service status changes in real-time.
     *
     * @return Flow of ServiceStatus updates
     */
    operator fun invoke(): Flow<ServiceStatus> {
        return wakeWordRepository.observeServiceStatus()
    }
}
