package com.ctrlbsketr.novaassistant.features.wakeword.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.core.ui.preview.LightPreview
import com.ctrlbsketr.novaassistant.core.ui.preview.DarkPreview
import com.ctrlbsketr.novaassistant.core.ui.preview.ThemePreviews
import kotlinx.coroutines.launch

/**
 * Visual feedback component that shows a ripple animation when wake word is detected.
 * Creates expanding concentric circles that fade out.
 * The animation automatically plays when this composable is first displayed.
 *
 * @param modifier Modifier for the component
 */
@Composable
fun WakeWordFeedback(
    modifier: Modifier = Modifier
) {
    RippleAnimation(modifier = modifier)
}

@Composable
private fun RippleAnimation(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    val animationDuration = 800
    val initialScale = remember { 0.8f }
    val targetScale = 2.5f
    val initialAlpha1 = remember { 0.8f }
    val initialAlpha2 = remember { 0.6f }
    val initialAlpha3 = remember { 0.4f }

    // First ripple - start from initial values
    val scale1 = remember { Animatable(initialScale) }
    val alpha1 = remember { Animatable(initialAlpha1) }
    
    LaunchedEffect(Unit) {
        launch {
            scale1.animateTo(
                targetValue = targetScale,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha1.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // Second ripple (delayed)
    val scale2 = remember { Animatable(initialScale) }
    val alpha2 = remember { Animatable(initialAlpha2) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        launch {
            scale2.animateTo(
                targetValue = targetScale,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha2.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // Third ripple (more delayed)
    val scale3 = remember { Animatable(initialScale) }
    val alpha3 = remember { Animatable(initialAlpha3) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        launch {
            scale3.animateTo(
                targetValue = targetScale,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha3.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(modifier = modifier.size(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val baseRadius = size.minDimension / 2.5f

            // Draw three expanding ripples
            drawCircle(
                color = color.copy(alpha = alpha1.value),
                radius = baseRadius * scale1.value,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = 4.dp.toPx())
            )

            drawCircle(
                color = color.copy(alpha = alpha2.value),
                radius = baseRadius * scale2.value,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = color.copy(alpha = alpha3.value),
                radius = baseRadius * scale3.value,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// ========================================
// Previews
// ========================================

/**
 * Wake word feedback animation - Light theme.
 * Shows ripple animation as it appears after wake word detection.
 */
@LightPreview
@Composable
private fun WakeWordFeedbackPreview_Light() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WakeWordFeedback()
        }
    }
}

/**
 * Wake word feedback animation - Dark theme.
 */
@DarkPreview
@Composable
private fun WakeWordFeedbackPreview_Dark() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WakeWordFeedback()
        }
    }
}

/**
 * Wake word feedback with status indicator overlay.
 * Shows how it looks in actual usage on the main screen.
 */
@ThemePreviews
@Composable
private fun WakeWordFeedbackPreview_WithStatusIndicator() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WakeWordFeedback()
            StatusIndicator(
                state = com.ctrlbsketr.novaassistant.features.assistant.domain.model.AssistantState.Processing
            )
        }
    }
}

/**
 * Wake word feedback on different device sizes.
 */
@Preview(
    name = "Phone - Portrait",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Preview(
    name = "Tablet - Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp"
)
@Composable
private fun WakeWordFeedbackPreview_Devices() {
    NovaAssistantTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WakeWordFeedback()
        }
    }
}
