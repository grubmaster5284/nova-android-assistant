package com.ctrlbsketr.novaassistant.features.assistant.presentation.main.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import com.ctrlbsketr.novaassistant.core.ui.preview.LightPreview
import com.ctrlbsketr.novaassistant.core.ui.preview.ThemePreviews

/**
 * Circular action button with subtle press animation and ripple.
 * Clean charcoal background with white icon.
 */
@Composable
fun CircularActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            stiffness = 300f,
            dampingRatio = 0.6f
        ),
        label = "button_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1.0f else 0.6f,
        label = "button_alpha"
    )

    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                color = Color(0xFF1F2937).copy(alpha = alpha), // Charcoal
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple handled by scale
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

// ========================================
// Previews
// ========================================

@ThemePreviews
@Composable
private fun CircularActionButtonPreview_Active() {
    NovaAssistantTheme {
        Surface {
            CircularActionButton(
                icon = Icons.Default.Mic,
                contentDescription = "Microphone",
                onClick = {},
                isActive = true
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CircularActionButtonPreview_Inactive() {
    NovaAssistantTheme {
        Surface {
            CircularActionButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = {},
                isActive = false
            )
        }
    }
}

@LightPreview
@Composable
private fun CircularActionButtonPreview_AllStates() {
    NovaAssistantTheme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                CircularActionButton(
                    icon = Icons.Default.Mic,
                    contentDescription = "Mic Active",
                    onClick = {},
                    isActive = true
                )
                CircularActionButton(
                    icon = Icons.Default.Stop,
                    contentDescription = "Stop Inactive",
                    onClick = {},
                    isActive = false
                )
                CircularActionButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = {},
                    isActive = true
                )
            }
        }
    }
}

