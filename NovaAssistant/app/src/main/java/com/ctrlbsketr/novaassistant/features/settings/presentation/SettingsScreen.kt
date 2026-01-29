package com.ctrlbsketr.novaassistant.features.settings.presentation

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctrlbsketr.novaassistant.R
import com.ctrlbsketr.novaassistant.features.assistant.presentation.main.MainViewModel
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.core.ui.preview.ThemePreviews
import com.ctrlbsketr.novaassistant.core.ui.preview.DevicePreviews
import kotlin.system.exitProcess

/**
 * Settings screen for Nova Assistant.
 * Displays information about the app, UI version picker, and exit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Nova is always listening for the wake word. " +
                                "When you say \"Hey Nova\", it will activate and be ready for your commands.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.company_name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Exit Button
            Button(
                onClick = { showExitDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit App")
            }
        }

        // Exit confirmation dialog
        if (showExitDialog) {
            ExitConfirmationDialog(
                onConfirm = {
                    // Stop service
                    viewModel.stopService()
                    // Exit app
                    (context as? Activity)?.finishAffinity()
                    exitProcess(0)
                },
                onDismiss = { showExitDialog = false }
            )
        }
    }
}

@Composable
private fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit Nova Assistant?") },
        text = { Text("This will stop the wake word service and close the app completely.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Settings content without ViewModel dependency for previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    onNavigateBack: () -> Unit,
    onExitClick: () -> Unit,
    companyName: String = "Nova Assistant"
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Nova is always listening for the wake word. " +
                                "When you say \"Hey Nova\", it will activate and be ready for your commands.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Exit Button
            Button(
                onClick = onExitClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit App")
            }
        }
    }
}

// ========================================
// Previews
// ========================================

@ThemePreviews
@Composable
private fun SettingsContentPreview() {
    NovaAssistantTheme {
        SettingsContent(
            onNavigateBack = {},
            onExitClick = {},
            companyName = "Nova Assistant"
        )
    }
}

@DevicePreviews
@Composable
private fun SettingsContentPreview_Devices() {
    NovaAssistantTheme {
        SettingsContent(
            onNavigateBack = {},
            onExitClick = {},
            companyName = "Nova Assistant"
        )
    }
}

@ThemePreviews
@Composable
private fun ExitConfirmationDialogPreview() {
    NovaAssistantTheme {
        ExitConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

