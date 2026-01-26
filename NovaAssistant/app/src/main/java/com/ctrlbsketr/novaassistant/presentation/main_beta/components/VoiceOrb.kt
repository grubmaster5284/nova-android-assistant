package com.ctrlbsketr.novaassistant.presentation.main_beta.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ctrlbsketr.novaassistant.domain.model.AssistantState
import com.ctrlbsketr.novaassistant.presentation.main_beta.components.animations.AssistantSpeakingAnimation
import com.ctrlbsketr.novaassistant.presentation.main_beta.components.animations.ProcessingAnimation
import com.ctrlbsketr.novaassistant.presentation.main_beta.components.animations.UserSpeakingAnimation
import kotlin.math.min

/**
 * Voice Orb - The central visual element of the Beta UI.
 *
 * Displays different animations based on the assistant state:
 * - Idle: Gentle breathing
 * - WakeDetected: Quick bounce
 * - UserSpeaking: Cyan ripples (outward)
 * - Processing: Purple swirl (rotating)
 * - AssistantSpeaking: Purple inner glow (pulsing)
 * - Error: Red tint with shake
 *
 * @param assistantState Current state of the assistant
 * @param modifier Modifier for positioning
 * @param size Size of the orb
 */
@Composable
fun VoiceOrb(
    assistantState: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    // Base orb colors (blue gradient)
    val baseGradientTop = Color(0xFFE0F2FE) // Light blue/white
    val baseGradientMid = Color(0xFF7DD3FC) // Sky blue
    val baseGradientBottom = Color(0xFF0284C7) // Deep blue

    // Animate scale for breathing and bounce effects
    val targetScale = when (assistantState) {
        is AssistantState.Idle -> 1.02f
        is AssistantState.WakeDetected -> 1.15f
        else -> 1.05f
    }

    val animationDuration = when (assistantState) {
        is AssistantState.Idle -> 3000
        is AssistantState.WakeDetected -> 400
        else -> 300
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (assistantState is AssistantState.Idle) {
            infiniteRepeatable(
                animation = tween(animationDuration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(animationDuration, easing = FastOutSlowInEasing)
        },
        label = "orb_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Base orb with gradient
        Canvas(
            modifier = Modifier
                .size(size * scale)
        ) {
            val centerX = this.size.width / 2
            val centerY = this.size.height / 2
            val radius = min(centerX, centerY)

            // Draw base orb with blue gradient
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    baseGradientTop,
                    baseGradientMid,
                    baseGradientBottom
                ),
                startY = 0f,
                endY = this.size.height
            )

            drawCircle(
                brush = gradient,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Add error overlay if in error state
            if (assistantState is AssistantState.Error) {
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.4f), // Red overlay
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }
        }

        // Overlay state-specific animations
        when (assistantState) {
            is AssistantState.UserSpeaking -> {
                UserSpeakingAnimation(
                    audioAmplitude = assistantState.audioAmplitude,
                    size = size
                )
            }

            is AssistantState.Processing -> {
                ProcessingAnimation(
                    size = size
                )
            }

            is AssistantState.AssistantSpeaking -> {
                AssistantSpeakingAnimation(
                    audioAmplitude = assistantState.audioAmplitude,
                    size = size
                )
            }

            else -> {
                // Idle, WakeDetected, or Error - no overlay animation
            }
        }
    }
}
