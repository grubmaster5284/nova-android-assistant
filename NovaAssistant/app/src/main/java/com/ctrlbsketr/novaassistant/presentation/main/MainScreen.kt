package com.ctrlbsketr.novaassistant.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctrlbsketr.novaassistant.domain.models.AssistantState
import com.ctrlbsketr.novaassistant.domain.models.Settings
import com.ctrlbsketr.novaassistant.domain.models.ServiceStatus
import com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent
import com.ctrlbsketr.novaassistant.presentation.components.*
import com.ctrlbsketr.novaassistant.presentation.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.util.SoundPlayer

/**
 * Main screen for Nova Assistant.
 * Shows service status, controls, and settings.
 *
 * Demonstrates:
 * - MVVM pattern with Compose
 * - State hoisting from ViewModel
 * - Composable UI components
 * - Material 3 design
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        wakeWordEvent?.let {
            if (uiState.settings.enableAudioFeedback) {
                SoundPlayer.playBing()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Assistant") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            MainContent(
                uiState = uiState,
                wakeWordEvent = wakeWordEvent,
                permissionsGranted = permissionsGranted,
                showPermissionError = showPermissionError,
                onClearError = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    wakeWordEvent: com.ctrlbsketr.novaassistant.domain.models.WakeWordEvent?,
    permissionsGranted: Boolean,
    showPermissionError: Boolean,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Wake word feedback overlay
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Visual feedback when wake word is detected
                wakeWordEvent?.let { event ->
                    // Use timestamp as key to trigger animation for each new event
                    key(event.timestamp) {
                        WakeWordFeedback(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                
                // Enhanced status indicator with microphone icon
                EnhancedStatusIndicator(
                    state = uiState.assistantState,
                    isListening = uiState.isServiceRunning && uiState.serviceStatus.isListening,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Service info
            if (uiState.isServiceRunning) {
                ServiceInfoCard(uiState = uiState)
            }
        }

        // Controls Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission error
            if (showPermissionError && !permissionsGranted) {
                PermissionErrorCard()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error message
            uiState.errorMessage?.let { error ->
                ErrorCard(
                    message = error,
                    onDismiss = onClearError
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info card about always-on listening
            if (permissionsGranted && uiState.isServiceRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Always Listening",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nova is continuously listening for \"${uiState.settings.wakeWord}\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceInfoCard(uiState: MainUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Service Active",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Wake word: \"${uiState.settings.wakeWord}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ToggleServiceButton(
    isRunning: Boolean,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        colors = if (isRunning) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (isRunning) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Listening")
            } else {
                Text("Start Listening")
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun PermissionErrorCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nova needs microphone and notification permissions to work. " +
                        "Please grant permissions in your device settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Start
            )
        }
    }
}

// ========================================
// Main Screen Previews
// ========================================

/**
 * Preview with all UI states using parameter provider.
 */
@Preview(
    name = "All UI States",
    showBackground = true,
    group = "Main Screen"
)
@Composable
private fun MainScreenPreview_AllStates(
    @PreviewParameter(MainUiStateProvider::class) uiState: MainUiState
) {
    NovaAssistantTheme {
        MainContent(
            uiState = uiState,
            wakeWordEvent = null,
            permissionsGranted = true,
            showPermissionError = false,
            onClearError = {}
        )
    }
}

/**
 * Complete preview with theme variations for listening state.
 */
@CompletePreviews
@Composable
private fun MainScreenPreview_Listening_Complete() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Listening,
                serviceStatus = ServiceStatus(isRunning = true, isListening = true),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            permissionsGranted = true,
            showPermissionError = false,
            onClearError = {}
        )
    }
}

/**
 * System UI preview showing full screen with status bar.
 */
@SystemUiPreview
@Composable
private fun MainScreenPreview_SystemUI() {
    NovaAssistantTheme {
        MainScreen(
            viewModel = hiltViewModel(),
            onNavigateToSettings = {}
        )
    }
}

/**
 * Accessibility preview with large font and error message.
 */
@AccessibilityPreviews
@Composable
private fun MainScreenPreview_Accessibility() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Error("Wake word detection failed. Please check microphone permissions."),
                errorMessage = "Wake word detection failed. Please check microphone permissions."
            ),
            wakeWordEvent = null,
            permissionsGranted = true,
            showPermissionError = false,
            onClearError = {}
        )
    }
}

// ========================================
// Component Previews
// ========================================

/**
 * Service info card with theme variations.
 */
@ThemePreviews
@Composable
private fun ServiceInfoCardPreview() {
    NovaAssistantTheme {
        ServiceInfoCard(
            uiState = MainUiState(
                serviceStatus = ServiceStatus(isRunning = true, isListening = true),
                settings = Settings.default()
            )
        )
    }
}

/**
 * Toggle button states with theme variations.
 */
@ThemePreviews
@Composable
private fun ToggleServiceButtonPreview_Start() {
    NovaAssistantTheme {
        ToggleServiceButton(
            isRunning = false,
            isLoading = false,
            enabled = true,
            onClick = {}
        )
    }
}

@ThemePreviews
@Composable
private fun ToggleServiceButtonPreview_Stop() {
    NovaAssistantTheme {
        ToggleServiceButton(
            isRunning = true,
            isLoading = false,
            enabled = true,
            onClick = {}
        )
    }
}

@LightPreview
@Composable
private fun ToggleServiceButtonPreview_Loading() {
    NovaAssistantTheme {
        ToggleServiceButton(
            isRunning = false,
            isLoading = true,
            enabled = true,
            onClick = {}
        )
    }
}

@LightPreview
@Composable
private fun ToggleServiceButtonPreview_Disabled() {
    NovaAssistantTheme {
        ToggleServiceButton(
            isRunning = false,
            isLoading = false,
            enabled = false,
            onClick = {}
        )
    }
}

/**
 * Error card with theme and accessibility previews.
 */
@ThemePreviews
@Composable
private fun ErrorCardPreview() {
    NovaAssistantTheme {
        ErrorCard(
            message = "Failed to initialize wake word detection. Please check your permissions.",
            onDismiss = {}
        )
    }
}

@AccessibilityPreviews
@Composable
private fun ErrorCardPreview_LongMessage() {
    NovaAssistantTheme {
        ErrorCard(
            message = "Wake word detection failed. The microphone permission was denied. Please go to Settings > Apps > Nova Assistant > Permissions and enable microphone access.",
            onDismiss = {}
        )
    }
}

/**
 * Permission error card with theme variations.
 */
@ThemePreviews
@Composable
private fun PermissionErrorCardPreview() {
    NovaAssistantTheme {
        PermissionErrorCard()
    }
}
