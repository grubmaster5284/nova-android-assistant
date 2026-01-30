package com.novaassistant.core.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotations for comprehensive UI testing.
 * Following latest Android Compose preview best practices.
 */

/**
 * Standard preview for light mode.
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
annotation class LightPreview

/**
 * Standard preview for dark mode.
 */
@Preview(
    name = "Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class DarkPreview

/**
 * Combined light and dark mode previews.
 */
@LightPreview
@DarkPreview
annotation class ThemePreviews

/**
 * Preview with different font scales for accessibility testing.
 */
@Preview(
    name = "Small Font",
    showBackground = true,
    fontScale = 0.85f
)
@Preview(
    name = "Normal Font",
    showBackground = true,
    fontScale = 1.0f
)
@Preview(
    name = "Large Font",
    showBackground = true,
    fontScale = 1.15f
)
@Preview(
    name = "Extra Large Font",
    showBackground = true,
    fontScale = 1.3f
)
annotation class FontScalePreviews

/**
 * Preview with different device sizes.
 */
@Preview(
    name = "Phone - Portrait",
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Preview(
    name = "Phone - Landscape",
    showBackground = true,
    device = "spec:width=891dp,height=411dp"
)
@Preview(
    name = "Tablet - Portrait",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240"
)
@Preview(
    name = "Tablet - Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
annotation class DevicePreviews

/**
 * Comprehensive preview combining theme and device variations.
 * Use for critical screens and components.
 */
@LightPreview
@DarkPreview
@Preview(
    name = "Tablet",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
annotation class CompletePreviews

/**
 * Preview for components with system UI (status bar, nav bar).
 */
@Preview(
    name = "With System UI",
    showBackground = true,
    showSystemUi = true
)
annotation class SystemUiPreview

/**
 * Combined accessibility previews (font scales + themes).
 */
@Preview(
    name = "Large Font - Light",
    showBackground = true,
    fontScale = 1.3f
)
@Preview(
    name = "Large Font - Dark",
    showBackground = true,
    fontScale = 1.3f,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class AccessibilityPreviews
