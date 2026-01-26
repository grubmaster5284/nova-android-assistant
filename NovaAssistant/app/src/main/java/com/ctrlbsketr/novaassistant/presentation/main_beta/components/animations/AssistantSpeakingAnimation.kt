package com.ctrlbsketr.novaassistant.presentation.main_beta.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Assistant speaking animation - displays inner glow pulsing effect.
 *
 * Features:
 * - Smooth breathing animation synchronized with TTS
 * - Inner glow intensity matches audio volume
 * - Warm purple gradient overlay
 * - Slower, more deliberate than user ripples
 *
 * @param audioAmplitude Current TTS audio amplitude (0.0 to 1.0)
 * @param modifier Modifier for sizing
 * @param size Size of the animation area
 * @param glowColor Base color of the glow effect
 */
@Composable
fun AssistantSpeakingAnimation(
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    glowColor: Color = Color(0xFFA855F7) // Warm purple
) {
    // Smooth pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_intensity"
    )

    // Combine base animation with audio amplitude
    val combinedIntensity = remember(glowIntensity, audioAmplitude) {
        (glowIntensity * 0.6f) + (audioAmplitude * 0.4f)
    }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val maxRadius = min(centerX, centerY)

            // Inner glow - multiple layers for smooth gradient
            val glowLayers = 3

            for (layer in 0 until glowLayers) {
                val layerProgress = (layer + 1) / glowLayers.toFloat()
                val radius = maxRadius * layerProgress * 0.8f
                val alpha = combinedIntensity * (1f - layerProgress) * 0.6f

                // Radial gradient for smooth glow
                val gradient = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = alpha),
                        glowColor.copy(alpha = alpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )

                drawCircle(
                    brush = gradient,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }

            // Central glow pulse
            val pulseRadius = maxRadius * 0.4f * combinedIntensity
            drawCircle(
                color = glowColor.copy(alpha = combinedIntensity * 0.7f),
                radius = pulseRadius,
                center = Offset(centerX, centerY)
            )
        }
    }
}
