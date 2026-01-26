package com.ctrlbsketr.novaassistant.presentation.main_beta.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * User speaking animation - displays audio-reactive ripples expanding outward.
 *
 * Features:
 * - Multiple concentric ripples (3-5)
 * - Ripples responsive to audio amplitude
 * - Cyan color overlay on blue gradient
 * - Fast, sharp outward motion
 *
 * @param audioAmplitude Current audio input amplitude (0.0 to 1.0)
 * @param modifier Modifier for sizing
 * @param size Size of the animation area
 * @param rippleColor Color of the ripples
 */
@Composable
fun UserSpeakingAnimation(
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    rippleColor: Color = Color(0xFF06B6D4) // Cyan
) {
    // Generate multiple ripple states
    val rippleCount = 5
    val rippleStates = remember { List(rippleCount) { mutableStateOf(0f) } }

    // Animate each ripple independently
    rippleStates.forEachIndexed { index, state ->
        val infiniteTransition = rememberInfiniteTransition(label = "ripple_$index")
        val rippleProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    delayMillis = index * 300, // Stagger each ripple
                    easing = LinearOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple_progress_$index"
        )

        LaunchedEffect(rippleProgress) {
            state.value = rippleProgress
        }
    }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val maxRadius = min(centerX, centerY)

            // Draw each ripple
            rippleStates.forEachIndexed { index, state ->
                val progress = state.value

                // Scale ripple based on audio amplitude
                val amplitudeMultiplier = 0.3f + (audioAmplitude * 0.7f)
                val radius = maxRadius * progress * amplitudeMultiplier * 2.0f

                // Fade out as ripple expands
                val alpha = (1f - progress) * 0.8f

                drawCircle(
                    color = rippleColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}
