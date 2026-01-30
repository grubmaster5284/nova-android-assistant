package com.novaassistant.features.assistant.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaassistant.core.theme.NovaAssistantTheme
import com.novaassistant.core.ui.preview.ThemePreviews
import com.novaassistant.core.ui.preview.AccessibilityPreviews
import com.novaassistant.core.ui.preview.LightPreview

/**
 * Lightweight error overlay shown at bottom with scrim.
 * Compact pill/bottom card style.
 */
@Composable
fun ErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.22f)) // Scrim
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1F2937), // Dark grey
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Permission error overlay.
 */
@Composable
fun PermissionErrorOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorOverlay(
        message = "Microphone and notification permissions are required.",
        onDismiss = onDismiss,
        modifier = modifier
    )
}

// ========================================
// Previews
// ========================================

@ThemePreviews
@Composable
private fun ErrorOverlayPreview_Short() {
    NovaAssistantTheme {
        Surface(color = Color.Black) {
            ErrorOverlay(
                message = "Wake word detection failed",
                onDismiss = {}
            )
        }
    }
}

@LightPreview
@Composable
private fun ErrorOverlayPreview_Long() {
    NovaAssistantTheme {
        Surface(color = Color.Black) {
            ErrorOverlay(
                message = "Wake word detection failed. Please check microphone permissions and try again.",
                onDismiss = {}
            )
        }
    }
}

@AccessibilityPreviews
@Composable
private fun ErrorOverlayPreview_Accessibility() {
    NovaAssistantTheme {
        Surface(color = Color.Black) {
            ErrorOverlay(
                message = "Microphone permission was denied. Please go to Settings > Apps > Nova Assistant to grant permission.",
                onDismiss = {}
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PermissionErrorOverlayPreview() {
    NovaAssistantTheme {
        Surface(color = Color.Black) {
            PermissionErrorOverlay(onDismiss = {})
        }
    }
}



