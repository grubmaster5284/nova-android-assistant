package com.ctrlbsketr.novaassistant.core.ui.components

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.core.ui.preview.ThemePreviews
import com.ctrlbsketr.novaassistant.core.ui.preview.AccessibilityPreviews
import com.ctrlbsketr.novaassistant.core.ui.preview.DevicePreviews

/**
 * Handles runtime permissions for Nova Assistant.
 * Uses Accompanist Permissions library for Compose-friendly permission handling.
 *
 * Required permissions:
 * - RECORD_AUDIO: For wake word detection and voice commands
 * - POST_NOTIFICATIONS: For foreground service notification
 * - READ_CONTACTS: For finding contacts (requested on first use)
 * - CALL_PHONE: For placing calls (requested on confirmation)
 *
 * Follows Android best practices:
 * - Request permissions at appropriate times
 * - Explain why permissions are needed
 * - Handle denial gracefully
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit = {}
) {
    val context = LocalContext.current

    // Core permissions needed to start the service
    val corePermissions = listOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )

    val permissionsState = rememberMultiplePermissionsState(
        permissions = corePermissions
    )

    var showRationale by remember { mutableStateOf(false) }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        }
    }

    // Show rationale dialog if needed
    if (showRationale) {
        PermissionRationaleDialog(
            onConfirm = {
                showRationale = false
                permissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                showRationale = false
                onPermissionsDenied()
            }
        )
    }

    // Request permissions or show rationale
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            if (permissionsState.shouldShowRationale) {
                showRationale = true
            } else {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
    }
}

@Composable
private fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Text(
                "Nova needs the following permissions to work:\n\n" +
                        "• Microphone: To listen for wake word and voice commands\n" +
                        "• Notifications: To show service status while running\n\n" +
                        "Your privacy is important. All processing happens on your device, " +
                        "and no audio is sent to the cloud."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permissions")
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
 * Request contacts permission when needed for calling.
 * Called separately from core permissions (on-demand).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactsPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.READ_CONTACTS
    )

    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> onGranted()
            permissionState.status.shouldShowRationale -> {
                // Show rationale and request
                permissionState.launchPermissionRequest()
            }
            else -> {
                permissionState.launchPermissionRequest()
            }
        }
    }
}

/**
 * Request phone call permission when user confirms a call.
 * Called separately (on-demand, before placing call).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCallPhonePermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CALL_PHONE
    )

    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> onGranted()
            permissionState.status.shouldShowRationale -> {
                permissionState.launchPermissionRequest()
            }
            else -> {
                permissionState.launchPermissionRequest()
            }
        }
    }
}

// ========================================
// Previews
// ========================================

/**
 * Permission rationale dialog with theme variations.
 */
@ThemePreviews
@Composable
private fun PermissionRationaleDialogPreview() {
    NovaAssistantTheme {
        PermissionRationaleDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

/**
 * Permission rationale dialog with accessibility preview.
 */
@AccessibilityPreviews
@Composable
private fun PermissionRationaleDialogPreview_Accessibility() {
    NovaAssistantTheme {
        PermissionRationaleDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

/**
 * Permission rationale dialog on different devices.
 */
@DevicePreviews
@Composable
private fun PermissionRationaleDialogPreview_Devices() {
    NovaAssistantTheme {
        PermissionRationaleDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
