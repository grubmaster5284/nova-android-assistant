package com.ctrlbsketr.novaassistant.features.assistant.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctrlbsketr.novaassistant.features.assistant.domain.model.AssistantState
import com.ctrlbsketr.novaassistant.core.ui.components.PermissionHandler
import com.ctrlbsketr.novaassistant.features.assistant.presentation.main.components.*
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.features.audio.domain.manager.SoundPlayer
import androidx.compose.ui.tooling.preview.Preview
import com.ctrlbsketr.novaassistant.features.settings.domain.model.Settings
import com.ctrlbsketr.novaassistant.features.wakeword.domain.model.ServiceStatus
import com.ctrlbsketr.novaassistant.core.ui.preview.CompletePreviews
import com.ctrlbsketr.novaassistant.core.ui.preview.DarkPreview

/**
 * Main Screen - Modern minimalist orb UI with state-driven animations.
 *
 * Features:
 * - Pure black background
 * - Large central gradient orb with modern animations
 * - Compact top control pill (settings, volume, captions)
 * - Status label below orb
 * - Bottom circular action buttons (mic trigger, close/stop)
 * - Lightweight permission/error overlays
 *
 * @param viewModel Main view model for state management
 * @param onNavigateToSettings Callback to navigate to settings
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wakeWordEvent by viewModel.wakeWordEvents.collectAsStateWithLifecycle()
    var permissionsGranted by remember { mutableStateOf(false) }
    var showPermissionError by remember { mutableStateOf(false) }

    // Auto-start service when permissions are granted
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted && !uiState.isServiceRunning && !uiState.isLoading) {
            viewModel.startService()
        }
    }

    // Play audio feedback when wake word is detected
    LaunchedEffect(wakeWordEvent?.timestamp) {
        wakeWordEvent?.let { event ->
            if (uiState.settings.enableAudioFeedback) {
                SoundPlayer.playBing()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Request permissions first
        if (!permissionsGranted) {
            PermissionHandler(
                onPermissionsGranted = {
                    permissionsGranted = true
                },
                onPermissionsDenied = {
                    showPermissionError = true
                }
            )
        }

        // Main content
        if (permissionsGranted) {
            MainContent(
                uiState = uiState,
                wakeWordEvent = wakeWordEvent,
                onNavigateToSettings = onNavigateToSettings,
                onToggleService = { viewModel.toggleService() },
                onStopService = { viewModel.stopService() },
                onClearError = { viewModel.clearError() }
            )
        }

        // Permission error overlay
        AnimatedVisibility(
            visible = showPermissionError && !permissionsGranted,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PermissionErrorOverlay(
                onDismiss = { showPermissionError = false }
            )
        }

        // Error overlay
        uiState.errorMessage?.let { error ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ErrorOverlay(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: com.ctrlbsketr.novaassistant.features.assistant.presentation.main.MainUiState,
    wakeWordEvent: com.ctrlbsketr.novaassistant.features.wakeword.domain.model.WakeWordEvent?,
    onNavigateToSettings: () -> Unit,
    onToggleService: () -> Unit,
    onStopService: () -> Unit,
    onClearError: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top controls pill
        TopControlsPill(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onSettingsClick = onNavigateToSettings
        )

        // Center orb section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Orb with animations
            ModernOrb(
                assistantState = uiState.assistantState,
                wakeWordEvent = wakeWordEvent,
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Status label
            StatusLabel(
                assistantState = uiState.assistantState,
                isServiceRunning = uiState.isServiceRunning
            )
        }

        // Bottom action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Microphone button (left) - toggle service
            CircularActionButton(
                icon = Icons.Default.Mic,
                contentDescription = if (uiState.isServiceRunning) "Stop listening" else "Start listening",
                onClick = onToggleService,
                isActive = uiState.isServiceRunning
            )

            // Close button (right) - stop service
            CircularActionButton(
                icon = Icons.Default.Close,
                contentDescription = "Stop service",
                onClick = onStopService,
                isActive = false
            )
        }
    }
}

// ========================================
// Previews
// ========================================

@DarkPreview
@Composable
private fun MainContentPreview_Idle() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Idle,
                serviceStatus = ServiceStatus(isRunning = false),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            onNavigateToSettings = {},
            onToggleService = {},
            onStopService = {},
            onClearError = {}
        )
    }
}

@DarkPreview
@Composable
private fun MainContentPreview_Listening() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Listening,
                serviceStatus = ServiceStatus(isRunning = true, isListening = true),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            onNavigateToSettings = {},
            onToggleService = {},
            onStopService = {},
            onClearError = {}
        )
    }
}

@DarkPreview
@Composable
private fun MainContentPreview_Processing() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Processing,
                serviceStatus = ServiceStatus(isRunning = true),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            onNavigateToSettings = {},
            onToggleService = {},
            onStopService = {},
            onClearError = {}
        )
    }
}

@DarkPreview
@Composable
private fun MainContentPreview_Error() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Error("Wake word detection failed"),
                serviceStatus = ServiceStatus(isRunning = false),
                settings = Settings.default(),
                errorMessage = "Wake word detection failed"
            ),
            wakeWordEvent = null,
            onNavigateToSettings = {},
            onToggleService = {},
            onStopService = {},
            onClearError = {}
        )
    }
}

@CompletePreviews
@Composable
private fun MainContentPreview_Complete() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Listening,
                serviceStatus = ServiceStatus(isRunning = true, isListening = true),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            onNavigateToSettings = {},
            onToggleService = {},
            onStopService = {},
            onClearError = {}
        )
    }
}

