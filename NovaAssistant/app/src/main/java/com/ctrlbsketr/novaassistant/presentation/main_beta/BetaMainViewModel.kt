package com.ctrlbsketr.novaassistant.presentation.main_beta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctrlbsketr.novaassistant.domain.model.AssistantState
import com.ctrlbsketr.novaassistant.domain.models.ServiceStatus
import com.ctrlbsketr.novaassistant.domain.models.Settings
import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.domain.usecases.GetSettingsUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.ObserveServiceStatusUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.ObserveWakeWordEventsUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.StartWakeWordDetectionUseCase
import com.ctrlbsketr.novaassistant.domain.usecases.StopWakeWordDetectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Beta Main Screen.
 * Manages assistant state with accurate state tracking for the voice orb UI.
 *
 * Features:
 * - Strict state transitions (no premature state changes)
 * - Audio amplitude tracking for animations
 * - Separate from original MainViewModel for isolation
 *
 * @param startWakeWordDetection Use case for starting detection
 * @param stopWakeWordDetection Use case for stopping detection
 * @param observeServiceStatus Use case for observing service status
 * @param observeWakeWordEvents Use case for observing wake word events
 * @param getSettings Use case for getting settings
 */
@HiltViewModel
class BetaMainViewModel @Inject constructor(
    private val startWakeWordDetection: StartWakeWordDetectionUseCase,
    private val stopWakeWordDetection: StopWakeWordDetectionUseCase,
    observeServiceStatus: ObserveServiceStatusUseCase,
    observeWakeWordEvents: ObserveWakeWordEventsUseCase,
    private val getSettings: GetSettingsUseCase
) : ViewModel() {

    // Internal mutable state
    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    private val _audioAmplitude = MutableStateFlow(0f)

    // Public immutable state
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    // Observe service status
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
    private val wakeWordEvents: StateFlow<WakeWordEvent?> = observeWakeWordEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Observe wake word events and update state accordingly
        viewModelScope.launch {
            wakeWordEvents.collect { event ->
                if (event != null && _assistantState.value is AssistantState.Idle) {
                    onWakeWordDetected()
                }
            }
        }
    }

    /**
     * Start wake word detection service.
     * Auto-called when app starts and permissions are granted.
     */
    fun startService() {
        if (serviceStatus.value.isRunning) return

        viewModelScope.launch {
            startWakeWordDetection()
                .onSuccess {
                    _assistantState.value = AssistantState.Idle
                }
                .onFailure { error ->
                    _assistantState.value = AssistantState.Error(
                        message = error.message ?: "Failed to start service"
                    )
                }
        }
    }

    /**
     * Stop current listening session (close button).
     * Wake word service continues in background.
     */
    fun stopListening() {
        // Return to idle state, but service keeps running
        _assistantState.value = AssistantState.Idle
        _audioAmplitude.value = 0f
    }

    /**
     * Manual trigger - skip wake word and start listening immediately.
     * Called when microphone button is pressed.
     */
    fun manualTrigger() {
        if (_assistantState.value is AssistantState.Idle) {
            onWakeWordDetected()
        }
    }

    /**
     * Interrupt assistant speaking (microphone button during TTS).
     */
    fun interruptAssistant() {
        if (_assistantState.value is AssistantState.AssistantSpeaking) {
            // TODO: Stop TTS playback
            _assistantState.value = AssistantState.Idle
            _audioAmplitude.value = 0f
        }
    }

    /**
     * Update audio amplitude for ripple/glow animations.
     * Called continuously while user/assistant is speaking.
     *
     * @param amplitude Audio level (0.0 to 1.0)
     */
    fun updateAudioAmplitude(amplitude: Float) {
        _audioAmplitude.value = amplitude.coerceIn(0f, 1f)

        // Update assistant state with amplitude
        when (val current = _assistantState.value) {
            is AssistantState.UserSpeaking -> {
                _assistantState.value = AssistantState.UserSpeaking(amplitude)
            }
            is AssistantState.AssistantSpeaking -> {
                _assistantState.value = AssistantState.AssistantSpeaking(
                    text = current.text,
                    audioAmplitude = amplitude
                )
            }
            else -> {
                // No-op for other states
            }
        }
    }

    /**
     * Handle wake word detected event.
     * Transitions: Idle → WakeDetected → UserSpeaking
     */
    private fun onWakeWordDetected() {
        viewModelScope.launch {
            // Transition to wake detected
            _assistantState.value = AssistantState.WakeDetected

            // Brief pause for animation (400ms)
            kotlinx.coroutines.delay(400)

            // Transition to user speaking
            _assistantState.value = AssistantState.UserSpeaking(audioAmplitude = 0f)

            // TODO: Start recording user speech
            // For now, simulate user speaking for 3 seconds
            kotlinx.coroutines.delay(3000)

            // Transition to processing
            _assistantState.value = AssistantState.Processing

            // TODO: Send to STT and LLM
            // For now, simulate processing for 2 seconds
            kotlinx.coroutines.delay(2000)

            // Transition to assistant speaking
            _assistantState.value = AssistantState.AssistantSpeaking(
                text = "This is a simulated response.",
                audioAmplitude = 0f
            )

            // TODO: Play TTS
            // For now, simulate speaking for 3 seconds
            kotlinx.coroutines.delay(3000)

            // Return to idle
            _assistantState.value = AssistantState.Idle
            _audioAmplitude.value = 0f
        }
    }

    /**
     * Handle Porcupine initialization error from broadcast.
     */
    fun onPorcupineError(errorMessage: String) {
        _assistantState.value = AssistantState.Error(message = errorMessage)
    }

    /**
     * Clear error and return to idle.
     */
    fun clearError() {
        _assistantState.value = AssistantState.Idle
    }
}
