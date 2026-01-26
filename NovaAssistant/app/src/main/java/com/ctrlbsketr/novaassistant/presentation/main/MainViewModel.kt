package com.ctrlbsketr.novaassistant.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctrlbsketr.novaassistant.domain.models.AssistantState
import com.ctrlbsketr.novaassistant.domain.models.ServiceStatus
import com.ctrlbsketr.novaassistant.domain.models.Settings
import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.domain.usecases.GetSettingsUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.ObserveServiceStatusUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.ObserveWakeWordEventsUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.StartWakeWordDetectionUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.StopWakeWordDetectionUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main screen.
 * Follows MVVM pattern with Clean Architecture.
 *
 * Demonstrates:
 * - Single Responsibility: Manages only main screen state
 * - Dependency Inversion: Depends on use cases (abstractions), not repositories
 * - Separation of Concerns: UI logic separate from business logic
 *
 * @param startWakeWordDetection Use case for starting detection
 * @param stopWakeWordDetection Use case for stopping detection
 * @param observeServiceStatus Use case for observing service status
 * @param observeWakeWordEvents Use case for observing wake word events
 * @param getSettings Use case for getting settings
 * @param updateSettings Use case for updating settings
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val startWakeWordDetection: StartWakeWordDetectionUseCase,
    private val stopWakeWordDetection: StopWakeWordDetectionUseCase,
    observeServiceStatus: ObserveServiceStatusUseCase,
    observeWakeWordEvents: ObserveWakeWordEventsUseCase,
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {

    // Internal mutable state
    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Public immutable state
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Observe service status from repository
    val serviceStatus: StateFlow<ServiceStatus> = observeServiceStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServiceStatus()
        )

    // Observe settings
    val settings: StateFlow<Settings> = getSettings.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings.default()
        )

    // Observe wake word events
    val wakeWordEvents: StateFlow<WakeWordEvent?> = observeWakeWordEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Combined UI state
    val uiState: StateFlow<MainUiState> = combine(
        assistantState,
        serviceStatus,
        settings,
        errorMessage
    ) { state, status, settings, error ->
        MainUiState(
            assistantState = state,
            serviceStatus = status,
            settings = settings,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    /**
     * Start wake word detection service.
     */
    fun startService() {
        viewModelScope.launch {
            _assistantState.value = AssistantState.Initializing
            _errorMessage.value = null

            startWakeWordDetection()
                .onSuccess {
                    _assistantState.value = AssistantState.Listening
                }
                .onFailure { error ->
                    _assistantState.value = AssistantState.Error(
                        message = error.message ?: "Failed to start service",
                        throwable = error
                    )
                    _errorMessage.value = error.message
                }
        }
    }

    /**
     * Stop wake word detection service.
     */
    fun stopService() {
        viewModelScope.launch {
            _assistantState.value = AssistantState.Stopping
            _errorMessage.value = null

            stopWakeWordDetection()
                .onSuccess {
                    _assistantState.value = AssistantState.Idle
                }
                .onFailure { error ->
                    _assistantState.value = AssistantState.Error(
                        message = error.message ?: "Failed to stop service",
                        throwable = error
                    )
                    _errorMessage.value = error.message
                }
        }
    }

    /**
     * Toggle service on/off.
     */
    fun toggleService() {
        if (serviceStatus.value.isRunning) {
            stopService()
        } else {
            startService()
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update settings.
     */
    fun updateSettings(newSettings: Settings) {
        viewModelScope.launch {
            updateSettings.invoke(newSettings)
        }
    }

    /**
     * Handle Porcupine initialization error from broadcast.
     * Following official demo pattern - called when service broadcasts error.
     */
    fun onPorcupineError(errorMessage: String) {
        _assistantState.value = AssistantState.Error(
            message = errorMessage,
            throwable = null
        )
        _errorMessage.value = errorMessage
    }
}

/**
 * UI state for the main screen.
 * Immutable data class for predictable state management.
 */
data class MainUiState(
    val assistantState: AssistantState = AssistantState.Idle,
    val serviceStatus: ServiceStatus = ServiceStatus(),
    val settings: Settings = Settings.default(),
    val errorMessage: String? = null
) {
    val isServiceRunning: Boolean
        get() = serviceStatus.isRunning

    val isLoading: Boolean
        get() = assistantState is AssistantState.Initializing ||
                assistantState is AssistantState.Stopping
}
