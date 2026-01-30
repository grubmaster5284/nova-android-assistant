package com.novaassistant.features.assistant.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaassistant.core.theme.NovaAssistantTheme
import com.novaassistant.core.ui.preview.ThemePreviews
import com.novaassistant.core.ui.preview.LightPreview

/**
 * Compact top control pill with settings, volume, and captions icons.
 * Dark grey rounded rectangle with white icons.
 */
@Composable
fun TopControlsPill(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFF1F2937), // Dark grey
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Closed captions icon (placeholder - "cc" text)
        Text(
            text = "cc",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable { /* TODO: Toggle captions */ }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        // Volume icon
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume",
            tint = Color.White,
            modifier = Modifier
                .size(18.dp)
                .clickable { /* TODO: Volume control */ }
        )

        // Settings icon
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onSettingsClick)
        )
    }
}

// ========================================
// Previews
// ========================================

@ThemePreviews
@Composable
private fun TopControlsPillPreview() {
    NovaAssistantTheme {
        Surface(color = Color.Black) {
            Box(modifier = Modifier.padding(16.dp)) {
                TopControlsPill(onSettingsClick = {})
            }
        }
    }
}

@LightPreview
@Composable
private fun TopControlsPillPreview_OnDarkBackground() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(32.dp)
        ) {
            TopControlsPill(onSettingsClick = {})
        }
    }
}



