package com.novaassistant.features.assistant.presentation.main.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novaassistant.features.assistant.domain.model.AssistantState
import com.novaassistant.features.wakeword.domain.model.WakeWordEvent
import com.novaassistant.core.theme.NovaAssistantTheme
import com.novaassistant.core.ui.preview.DarkPreview
import com.novaassistant.core.ui.preview.LightPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Modern orb component with state-driven gradients and animations.
 *
 * Features:
 * - Base orb with gradient (blue/white)
 * - State-driven gradient shifts
 * - Springy micro-pulses for idle/listening
 * - Wake ping animation (single pulse on wake word)
 * - Low-opacity rim sweep
 * - Ambient glow layers
 */
@Composable
fun ModernOrb(
    assistantState: AssistantState,
    wakeWordEvent: WakeWordEvent?,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    // State-driven gradient colors
    val (topColor, midColor, bottomColor, overlayColor) = when (assistantState) {
        is AssistantState.Idle -> OrbColors.idle
        is AssistantState.Listening -> OrbColors.listening
        is AssistantState.Processing -> OrbColors.processing
        is AssistantState.Error -> OrbColors.error
        else -> OrbColors.idle
    }

    // Base pulse animation (minimal breathing)
    val shouldPulse = assistantState is AssistantState.Idle || 
                      assistantState is AssistantState.Listening ||
                      assistantState is AssistantState.Processing

    val pulseAnimationSpec = remember(shouldPulse) {
        if (shouldPulse) {
            infiniteRepeatable<Float>(
                animation = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            infiniteRepeatable<Float>(
                animation = tween(durationMillis = 0),
                repeatMode = RepeatMode.Reverse
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = pulseAnimationSpec,
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = pulseAnimationSpec,
        label = "pulse_alpha"
    )

    // Wake ping animation
    var wakePingTrigger by remember { mutableStateOf(0L) }
    LaunchedEffect(wakeWordEvent?.timestamp) {
        wakeWordEvent?.let {
            wakePingTrigger = it.timestamp
        }
    }

    val wakePingAnimatable = remember { Animatable(1.0f) }
    val wakePingAlpha = remember { Animatable(0.0f) }

    LaunchedEffect(wakePingTrigger) {
        if (wakePingTrigger > 0) {
            // Reset and animate ping
            wakePingAnimatable.snapTo(1.0f)
            wakePingAlpha.snapTo(0.35f)
            
            // Animate both simultaneously
            kotlinx.coroutines.coroutineScope {
                launch {
                    wakePingAnimatable.animateTo(
                        targetValue = 1.7f,
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    )
                }
                
                launch {
                    wakePingAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    )
                }
            }
        }
    }

    // Rim sweep animation
    val rimSweepTransition = rememberInfiniteTransition(label = "rim_sweep")
    val rimSweepAngle by rimSweepTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (assistantState is AssistantState.Processing) 2000 else 4000,
                easing = LinearEasing
            )
        ),
        label = "rim_angle"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size * pulseScale)
        ) {
            val centerX = this.size.width / 2
            val centerY = this.size.height / 2
            val radius = min(centerX, centerY)

            // Ambient glow layer (outer)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topColor.copy(alpha = 0.15f * pulseAlpha),
                        Color.Transparent
                    ),
                    radius = radius * 1.4f
                ),
                radius = radius * 1.3f,
                center = Offset(centerX, centerY)
            )

            // Base orb gradient
            val baseGradient = Brush.verticalGradient(
                colors = listOf(
                    topColor.copy(alpha = pulseAlpha),
                    midColor.copy(alpha = pulseAlpha),
                    bottomColor.copy(alpha = pulseAlpha)
                ),
                startY = 0f,
                endY = this.size.height
            )

            drawCircle(
                brush = baseGradient,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Overlay for processing/error states
            if (overlayColor != Color.Transparent) {
                drawCircle(
                    color = overlayColor,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }

            // Rim sweep (low opacity) - rotating arc
            if (shouldPulse) {
                rotate(rimSweepAngle, Offset(centerX, centerY)) {
                    // Draw a small arc segment on the rim
                    val rimRadius = radius * 1.05f
                    drawArc(
                        color = topColor.copy(alpha = 0.18f),
                        startAngle = -15f,
                        sweepAngle = 30f,
                        useCenter = false,
                        topLeft = Offset(centerX - rimRadius, centerY - rimRadius),
                        size = androidx.compose.ui.geometry.Size(rimRadius * 2f, rimRadius * 2f),
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Wake ping overlay
            if (wakePingAlpha.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            topColor.copy(alpha = wakePingAlpha.value),
                            Color.Transparent
                        ),
                        radius = radius * wakePingAnimatable.value
                    ),
                    radius = radius * wakePingAnimatable.value,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

/**
 * Orb color palettes for different states.
 */
private object OrbColors {
    // Idle: blue haze
    val idle = Quadruple(
        Color(0xFF9FD7FF), // top
        Color(0xFF6BB6FF), // mid
        Color(0xFF3A8DFF), // bottom
        Color.Transparent   // overlay
    )

    // Listening: brighter cyan edge glow
    val listening = Quadruple(
        Color(0xFFB8FFFC), // top
        Color(0xFF7DD3FC), // mid
        Color(0xFF4BA3FF), // bottom
        Color.Transparent   // overlay
    )

    // Processing: add faint violet film
    val processing = Quadruple(
        Color(0xFFC7B8FF), // top
        Color(0xFF9FD7FF), // mid
        Color(0xFF6BB6FF), // bottom
        Color(0xFFC7B8FF).copy(alpha = 0.18f) // overlay
    )

    // Error: amber-to-rose gradient
    val error = Quadruple(
        Color(0xFFFFB067), // top
        Color(0xFFFF8C6B), // mid
        Color(0xFFFF6B6B), // bottom
        Color(0xFFFF6B6B).copy(alpha = 0.25f) // overlay
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component1() = first
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component2() = second
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component3() = third
private operator fun <A, B, C, D> Quadruple<A, B, C, D>.component4() = fourth

// ========================================
// Previews
// ========================================

@DarkPreview
@Composable
private fun ModernOrbPreview_Idle() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            ModernOrb(
                assistantState = AssistantState.Idle,
                wakeWordEvent = null,
                size = 200.dp
            )
        }
    }
}

@DarkPreview
@Composable
private fun ModernOrbPreview_Listening() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            ModernOrb(
                assistantState = AssistantState.Listening,
                wakeWordEvent = null,
                size = 200.dp
            )
        }
    }
}

@DarkPreview
@Composable
private fun ModernOrbPreview_Processing() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            ModernOrb(
                assistantState = AssistantState.Processing,
                wakeWordEvent = null,
                size = 200.dp
            )
        }
    }
}

@DarkPreview
@Composable
private fun ModernOrbPreview_Error() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            ModernOrb(
                assistantState = AssistantState.Error("Error"),
                wakeWordEvent = null,
                size = 200.dp
            )
        }
    }
}

@LightPreview
@Composable
private fun ModernOrbPreview_AllStates() {
    NovaAssistantTheme {
        Row(
            modifier = Modifier
                .background(Color.Black)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernOrb(
                assistantState = AssistantState.Idle,
                wakeWordEvent = null,
                size = 100.dp
            )
            ModernOrb(
                assistantState = AssistantState.Listening,
                wakeWordEvent = null,
                size = 100.dp
            )
            ModernOrb(
                assistantState = AssistantState.Processing,
                wakeWordEvent = null,
                size = 100.dp
            )
            ModernOrb(
                assistantState = AssistantState.Error("Error"),
                wakeWordEvent = null,
                size = 100.dp
            )
        }
    }
}

