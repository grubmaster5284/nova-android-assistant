package com.ctrlbsketr.novaassistant.presentation.main_beta.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Typewriter effect text component.
 * Displays text character by character with a blinking cursor during typing.
 *
 * @param text The full text to display
 * @param modifier Modifier for styling
 * @param typingDelayMs Delay between each character in milliseconds
 * @param textStyle Style for the text
 * @param color Text color
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    typingDelayMs: Long = 50L,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        textAlign = TextAlign.Center
    ),
    color: Color = Color(0xFF9CA3AF) // Light gray
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var isTyping by remember(text) { mutableStateOf(true) }

    // Blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // Typewriter effect
    LaunchedEffect(text) {
        displayedText = ""
        isTyping = true

        text.forEachIndexed { index, _ ->
            displayedText = text.substring(0, index + 1)
            delay(typingDelayMs)
        }

        isTyping = false
    }

    // Display text with cursor
    val displayTextWithCursor = if (isTyping) {
        buildString {
            append(displayedText)
            // Add cursor with current alpha
            if (cursorAlpha > 0.5f) append("_")
        }
    } else {
        displayedText
    }

    Text(
        text = displayTextWithCursor,
        style = textStyle,
        color = color,
        modifier = modifier
    )
}
