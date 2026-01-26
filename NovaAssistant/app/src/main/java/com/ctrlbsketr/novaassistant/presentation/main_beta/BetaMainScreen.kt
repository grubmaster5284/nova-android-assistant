package com.ctrlbsketr.novaassistant.presentation.main_beta

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctrlbsketr.novaassistant.domain.model.AssistantState
import com.ctrlbsketr.novaassistant.domain.model.StatusMessage
import com.ctrlbsketr.novaassistant.presentation.main_beta.audio.AudioFeedbackManager
import com.ctrlbsketr.novaassistant.presentation.main_beta.components.TypewriterText
import com.ctrlbsketr.novaassistant.presentation.main_beta.components.VoiceOrb
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Beta Main Screen - Minimalist voice orb UI.
 *
 * Features:
 * - Pure black background
 * - Large central voice orb with state-driven animations
 * - Typewriter status text below orb
 * - Settings button (top right)
 * - Microphone button (bottom left) - manual trigger/interrupt
 * - Close button (bottom right) - stop listening
 *
 * @param viewModel Beta main view model
 * @param onNavigateToSettings Callback to navigate to settings
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BetaMainScreen(
    viewModel: BetaMainViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var permissionsGranted by remember { mutableStateOf(false) }

    // Permission handling
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    // Auto-start service when permissions granted
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            viewModel.startService()
        }
    }

    // Request permissions on launch
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            permissionsGranted = true
        }
    }

    // Play beep when wake word detected
    LaunchedEffect(assistantState) {
        when (assistantState) {
            is AssistantState.WakeDetected -> {
                // TODO: Play beep sound via AudioFeedbackManager
                // audioFeedbackManager?.playWakeWordBeep()
            }
            is AssistantState.Error -> {
                // TODO: Play error beep
                // audioFeedbackManager?.playErrorBeep()
            }
            else -> {
                // No audio feedback needed
            }
        }
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Pure black background
    ) {
        // Settings button (top right)
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = Color(0xFF1F2937), // Dark gray
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Voice Orb
            VoiceOrb(
                assistantState = assistantState,
                size = 300.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Typewriter status text
            TypewriterText(
                text = StatusMessage.fromAssistantState(assistantState).text,
                typingDelayMs = 50L,
                color = Color(0xFF9CA3AF) // Light gray
            )
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Microphone button (left)
            FloatingActionButton(
                onClick = {
                    when (assistantState) {
                        is AssistantState.Idle -> {
                            // Manual trigger - skip wake word
                            viewModel.manualTrigger()
                        }
                        is AssistantState.UserSpeaking -> {
                            // Stop recording early (optional feature)
                            // For now, just indicate active
                        }
                        is AssistantState.AssistantSpeaking -> {
                            // Interrupt assistant
                            viewModel.interruptAssistant()
                        }
                        else -> {
                            // No action in other states
                        }
                    }
                },
                containerColor = Color(0xFF1F2937), // Dark gray
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone"
                )
            }

            // Close button (right)
            FloatingActionButton(
                onClick = {
                    viewModel.stopListening()
                },
                containerColor = Color(0xFF1F2937), // Dark gray
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        // Permission denied message
        if (!permissionsGranted && permissionsState.shouldShowRationale) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Permissions Required") },
                text = { Text("Microphone and notification permissions are required for Nova Assistant to work.") },
                confirmButton = {
                    TextButton(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permissions")
                    }
                }
            )
        }
    }
}
