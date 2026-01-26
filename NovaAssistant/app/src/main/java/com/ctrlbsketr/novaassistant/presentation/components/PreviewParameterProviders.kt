package com.ctrlbsketr.novaassistant.presentation.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ctrlbsketr.novaassistant.domain.models.AssistantState
import com.ctrlbsketr.novaassistant.domain.models.ServiceStatus
import com.ctrlbsketr.novaassistant.domain.models.Settings
import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.presentation.main.MainUiState

/**
 * Preview parameter providers for consistent test data across previews.
 * Following Android Compose preview best practices for complex state.
 */

/**
 * Provides various AssistantState instances for previews.
 */
class AssistantStateProvider : PreviewParameterProvider<AssistantState> {
    override val values: Sequence<AssistantState> = sequenceOf(
        AssistantState.Idle,
        AssistantState.Initializing,
        AssistantState.Listening,
        AssistantState.Processing,
        AssistantState.Stopping,
        AssistantState.Error("Failed to initialize wake word detection"),
        AssistantState.Error("Microphone permission denied", RuntimeException("Permission denied"))
    )
}

/**
 * Provides various Settings instances for previews.
 */
class SettingsProvider : PreviewParameterProvider<Settings> {
    override val values: Sequence<Settings> = sequenceOf(
        Settings.default(),
        Settings(
            isServiceEnabled = true,
            wakeSensitivity = 0.3f,
            wakeWord = "hey nova",
            enableAudioFeedback = true,
            enableVisualFeedback = true
        ),
        Settings(
            isServiceEnabled = true,
            wakeSensitivity = 0.7f,
            wakeWord = "hey nova",
            enableAudioFeedback = false,
            enableVisualFeedback = true
        ),
        Settings(
            isServiceEnabled = false,
            wakeSensitivity = 0.5f,
            wakeWord = "hey nova",
            enableAudioFeedback = false,
            enableVisualFeedback = false
        )
    )
}

/**
 * Provides various ServiceStatus instances for previews.
 */
class ServiceStatusProvider : PreviewParameterProvider<ServiceStatus> {
    override val values: Sequence<ServiceStatus> = sequenceOf(
        ServiceStatus(
            isRunning = false,
            isListening = false,
            batteryImpact = 0f,
            detectionCount = 0,
            startTime = null
        ),
        ServiceStatus(
            isRunning = true,
            isListening = true,
            batteryImpact = 2.5f,
            detectionCount = 0,
            startTime = System.currentTimeMillis()
        ),
        ServiceStatus(
            isRunning = true,
            isListening = true,
            batteryImpact = 3.2f,
            detectionCount = 5,
            startTime = System.currentTimeMillis() - 3600000L // 1 hour ago
        ),
        ServiceStatus(
            isRunning = true,
            isListening = false,
            batteryImpact = 1.8f,
            detectionCount = 12,
            startTime = System.currentTimeMillis() - 7200000L // 2 hours ago
        )
    )
}

/**
 * Provides various WakeWordEvent instances for previews.
 */
class WakeWordEventProvider : PreviewParameterProvider<WakeWordEvent> {
    override val values: Sequence<WakeWordEvent> = sequenceOf(
        WakeWordEvent(
            keyword = "hey nova",
            confidence = 0.95f,
            timestamp = System.currentTimeMillis()
        ),
        WakeWordEvent(
            keyword = "hey nova",
            confidence = 0.72f,
            timestamp = System.currentTimeMillis()
        ),
        WakeWordEvent(
            keyword = "hey nova",
            confidence = 0.55f,
            timestamp = System.currentTimeMillis()
        )
    )
}

/**
 * Provides various MainUiState instances for comprehensive screen previews.
 */
class MainUiStateProvider : PreviewParameterProvider<MainUiState> {
    override val values: Sequence<MainUiState> = sequenceOf(
        // Idle state
        MainUiState(
            assistantState = AssistantState.Idle,
            serviceStatus = ServiceStatus(),
            settings = Settings.default(),
            errorMessage = null
        ),
        // Listening state
        MainUiState(
            assistantState = AssistantState.Listening,
            serviceStatus = ServiceStatus(
                isRunning = true,
                isListening = true,
                startTime = System.currentTimeMillis()
            ),
            settings = Settings.default(),
            errorMessage = null
        ),
        // Processing state
        MainUiState(
            assistantState = AssistantState.Processing,
            serviceStatus = ServiceStatus(
                isRunning = true,
                isListening = true,
                detectionCount = 1,
                startTime = System.currentTimeMillis()
            ),
            settings = Settings.default(),
            errorMessage = null
        ),
        // Error state
        MainUiState(
            assistantState = AssistantState.Error("Failed to start wake word detection"),
            serviceStatus = ServiceStatus(),
            settings = Settings.default(),
            errorMessage = "Failed to start wake word detection"
        ),
        // Initializing state
        MainUiState(
            assistantState = AssistantState.Initializing,
            serviceStatus = ServiceStatus(),
            settings = Settings.default(),
            errorMessage = null
        ),
        // Long-running service
        MainUiState(
            assistantState = AssistantState.Listening,
            serviceStatus = ServiceStatus(
                isRunning = true,
                isListening = true,
                batteryImpact = 3.5f,
                detectionCount = 25,
                startTime = System.currentTimeMillis() - 14400000L // 4 hours ago
            ),
            settings = Settings(
                isServiceEnabled = true,
                wakeSensitivity = 0.7f,
                wakeWord = "hey nova",
                enableAudioFeedback = true,
                enableVisualFeedback = true
            ),
            errorMessage = null
        )
    )
}
