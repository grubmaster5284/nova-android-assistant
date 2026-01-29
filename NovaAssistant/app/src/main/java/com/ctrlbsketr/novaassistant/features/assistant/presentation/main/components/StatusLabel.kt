package com.ctrlbsketr.novaassistant.features.assistant.presentation.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ctrlbsketr.novaassistant.features.assistant.domain.model.AssistantState

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

