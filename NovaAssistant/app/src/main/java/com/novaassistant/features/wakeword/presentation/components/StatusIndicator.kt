package com.novaassistant.features.wakeword.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.novaassistant.features.assistant.domain.model.AssistantState
import com.novaassistant.core.theme.NovaAssistantTheme
import com.novaassistant.core.ui.preview.ThemePreviews
import com.novaassistant.core.ui.preview.AccessibilityPreviews
import com.novaassistant.core.ui.preview.LightPreview
import com.novaassistant.core.ui.preview.AssistantStateProvider

/**
 * Visual status indicator for the assistant state.
 * Shows animated circle that pulses when listening.
 *
 * Demonstrates:
 * - Single Responsibility: Only renders status indicator
 * - Reusability: Can be used in different screens
 * - Composability: Pure Compose function
 */
@Composable
fun StatusIndicator(
    state: AssistantState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated circle
        AnimatedStatusCircle(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // Status text
        Text(
            text = getStatusText(state),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = getStatusColor(state)
        )

        // Additional info
        if (state is AssistantState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AnimatedStatusCircle(
    state: AssistantState,
    modifier: Modifier = Modifier
) {
    val color = getStatusColor(state)
    val shouldAnimate = state is AssistantState.Listening

    // Pulse animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldAnimate) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldAnimate) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2 * scale
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                style = Stroke(width = 8.dp.toPx())
            )
        }
    }
}

private fun getStatusText(state: AssistantState): String {
    return when (state) {
        AssistantState.Idle -> "Ready"
        AssistantState.Initializing -> "Starting..."
        AssistantState.Listening -> "Listening"
        AssistantState.Processing -> "Processing"
        is AssistantState.Error -> "Error"
        AssistantState.Stopping -> "Stopping..."
    }
}

@Composable
private fun getStatusColor(state: AssistantState): Color {
    return when (state) {
        AssistantState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        AssistantState.Initializing -> MaterialTheme.colorScheme.tertiary
        AssistantState.Listening -> MaterialTheme.colorScheme.primary
        AssistantState.Processing -> MaterialTheme.colorScheme.secondary
        is AssistantState.Error -> MaterialTheme.colorScheme.error
        AssistantState.Stopping -> MaterialTheme.colorScheme.tertiary
    }
}

// ========================================
// Previews
// ========================================

/**
 * Preview with all assistant states using parameter provider.
 */
@Preview(
    name = "All States",
    showBackground = true,
    group = "States"
)
@Composable
private fun StatusIndicatorPreview_AllStates(
    @PreviewParameter(AssistantStateProvider::class) state: AssistantState
) {
    NovaAssistantTheme {
        StatusIndicator(state = state)
    }
}

/**
 * Theme previews for listening state (most important).
 */
@ThemePreviews
@Composable
private fun StatusIndicatorPreview_Listening_Themes() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Listening)
    }
}

/**
 * Accessibility preview with large font.
 */
@AccessibilityPreviews
@Composable
private fun StatusIndicatorPreview_Accessibility() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Error("Wake word detection failed. Please check microphone permissions."))
    }
}

/**
 * Individual state previews for quick reference.
 */
@LightPreview
@Composable
private fun StatusIndicatorPreview_Idle() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Idle)
    }
}

@LightPreview
@Composable
private fun StatusIndicatorPreview_Listening() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Listening)
    }
}

@LightPreview
@Composable
private fun StatusIndicatorPreview_Processing() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Processing)
    }
}

@LightPreview
@Composable
private fun StatusIndicatorPreview_Error() {
    NovaAssistantTheme {
        StatusIndicator(state = AssistantState.Error("Sample error message"))
    }
}

/**
 * Enhanced status indicator with microphone icon.
 * Shows microphone icon when listening, with pulsing animation.
 */
@Composable
fun EnhancedStatusIndicator(
    state: AssistantState,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated microphone icon with pulsing circle
        AnimatedMicrophoneIndicator(
            state = state,
            isListening = isListening
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status text
        Text(
            text = getStatusText(state),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = getStatusColor(state)
        )

        // Additional info
        if (state is AssistantState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AnimatedMicrophoneIndicator(
    state: AssistantState,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val color = getStatusColor(state)
    val shouldAnimate = isListening && state is AssistantState.Listening

    // Pulse animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldAnimate) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldAnimate) 0.7f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing circle
        if (shouldAnimate) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2 * scale
                drawCircle(
                    color = color.copy(alpha = alpha * 0.3f),
                    radius = radius,
                    style = Stroke(width = 6.dp.toPx())
                )
            }
        }

        // Inner circle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2 * 0.7f
            drawCircle(
                color = color.copy(alpha = if (shouldAnimate) alpha * 0.2f else 0.1f),
                radius = radius
            )
        }

        // Microphone icon
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = if (isListening) "Listening" else "Microphone",
            modifier = Modifier.size(64.dp),
            tint = color.copy(alpha = alpha)
        )
    }
}
