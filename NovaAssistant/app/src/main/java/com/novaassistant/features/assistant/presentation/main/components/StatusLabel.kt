package com.novaassistant.features.assistant.presentation.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaassistant.features.assistant.domain.model.AssistantState
import com.novaassistant.core.theme.NovaAssistantTheme
import com.novaassistant.core.ui.preview.DarkPreview
import com.novaassistant.core.ui.preview.LightPreview

/**
 * Status label below the orb with animated crossfade transitions.
 * Single-line muted text showing current assistant state.
 */
@Composable
fun StatusLabel(
    assistantState: AssistantState,
    isServiceRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        !isServiceRunning -> "Idle"
        assistantState is AssistantState.Listening -> "Listening…"
        assistantState is AssistantState.Processing -> "Processing…"
        assistantState is AssistantState.Error -> "Error"
        else -> "Ready"
    }

    AnimatedContent(
        targetState = statusText,
        transitionSpec = {
            (fadeIn() + slideInVertically { -it }) togetherWith
            (fadeOut() + slideOutVertically { it })
        },
        modifier = modifier,
        label = "status_label"
    ) { text ->
        Text(
            text = text,
            color = Color(0xFF9CA3AF), // Light grey
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}

// ========================================
// Previews
// ========================================

@DarkPreview
@Composable
private fun StatusLabelPreview_Idle() {
    NovaAssistantTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            StatusLabel(
                assistantState = AssistantState.Idle,
                isServiceRunning = false
            )
        }
    }
}

@DarkPreview
@Composable
private fun StatusLabelPreview_Ready() {
    NovaAssistantTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            StatusLabel(
                assistantState = AssistantState.Idle,
                isServiceRunning = true
            )
        }
    }
}

@DarkPreview
@Composable
private fun StatusLabelPreview_Listening() {
    NovaAssistantTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            StatusLabel(
                assistantState = AssistantState.Listening,
                isServiceRunning = true
            )
        }
    }
}

@DarkPreview
@Composable
private fun StatusLabelPreview_Processing() {
    NovaAssistantTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            StatusLabel(
                assistantState = AssistantState.Processing,
                isServiceRunning = true
            )
        }
    }
}

@LightPreview
@Composable
private fun StatusLabelPreview_AllStates() {
    NovaAssistantTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusLabel(AssistantState.Idle, false)
            StatusLabel(AssistantState.Idle, true)
            StatusLabel(AssistantState.Listening, true)
            StatusLabel(AssistantState.Processing, true)
            StatusLabel(AssistantState.Error("Error"), true)
        }
    }
}

