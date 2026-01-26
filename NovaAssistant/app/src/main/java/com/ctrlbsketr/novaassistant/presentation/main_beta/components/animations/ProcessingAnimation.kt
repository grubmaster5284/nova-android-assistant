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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Processing animation - displays swirl effect with purple hints.
 *
 * Features:
 * - Clockwise 360° rotation
 * - Gradient morphing with purple overlay
 * - Continuous loop (2s duration)
 * - Shows active processing state
 *
 * @param modifier Modifier for sizing
 * @param size Size of the animation area
 * @param swirlColor Color of the swirl effect
 */
@Composable
fun ProcessingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    swirlColor: Color = Color(0xFF8B5CF6) // Purple
) {
    // Rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "swirl_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing intensity for extra depth
    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val maxRadius = min(centerX, centerY)

            rotate(degrees = rotation, pivot = Offset(centerX, centerY)) {
                // Draw swirl arcs
                val arcCount = 3

                for (i in 0 until arcCount) {
                    val angle = (i * 120f) // Evenly spaced
                    val radiusStart = maxRadius * 0.3f
                    val radiusEnd = maxRadius * 0.7f

                    // Calculate arc positions
                    val startAngle = angle
                    val sweepAngle = 80f

                    // Create path for swirl arc
                    val path = Path().apply {
                        val points = 20
                        val angleStep = sweepAngle / points

                        for (p in 0..points) {
                            val currentAngle = Math.toRadians((startAngle + angleStep * p).toDouble())
                            val radius = radiusStart + (radiusEnd - radiusStart) * (p / points.toFloat())
                            val x = centerX + (radius * cos(currentAngle)).toFloat()
                            val y = centerY + (radius * sin(currentAngle)).toFloat()

                            if (p == 0) {
                                moveTo(x, y)
                            } else {
                                lineTo(x, y)
                            }
                        }
                    }

                    // Gradient for swirl
                    val gradient = Brush.linearGradient(
                        colors = listOf(
                            swirlColor.copy(alpha = 0.2f * intensity),
                            swirlColor.copy(alpha = 0.6f * intensity),
                            swirlColor.copy(alpha = 0.2f * intensity)
                        ),
                        start = Offset(centerX - maxRadius, centerY),
                        end = Offset(centerX + maxRadius, centerY)
                    )

                    // Draw the swirl
                    drawPath(
                        path = path,
                        brush = gradient
                    )
                }

                // Central glowing core
                drawCircle(
                    color = swirlColor.copy(alpha = 0.5f * intensity),
                    radius = maxRadius * 0.2f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}
