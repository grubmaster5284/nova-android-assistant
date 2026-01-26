# Compose Preview System - Nova Assistant

This document explains the comprehensive preview system implemented for Nova Assistant UI components.

## Overview

Nova Assistant uses a sophisticated Compose preview system following Android's latest best practices for UI development and testing. The preview system enables:

- **Fast iteration** - See UI changes instantly without building the app
- **State testing** - Test all possible UI states in isolation
- **Accessibility** - Verify UI works with large fonts and different themes
- **Device compatibility** - Preview on phones, tablets, landscape/portrait
- **Dark mode** - Ensure proper theming in light and dark modes

## Multi-Preview Annotations

Located in `PreviewConfiguration.kt`, these annotations provide reusable preview configurations:

### Basic Theme Previews

```kotlin
@LightPreview        // Single light mode preview
@DarkPreview         // Single dark mode preview
@ThemePreviews       // Both light and dark mode
```

**Usage:**
```kotlin
@ThemePreviews
@Composable
private fun MyComponentPreview() {
    NovaAssistantTheme {
        MyComponent()
    }
}
```

### Accessibility Previews

```kotlin
@FontScalePreviews        // Small (0.85x), Normal (1.0x), Large (1.15x), Extra Large (1.3x)
@AccessibilityPreviews    // Large font in light + dark themes
```

**Usage:**
```kotlin
@AccessibilityPreviews
@Composable
private fun MyComponentPreview_Accessibility() {
    NovaAssistantTheme {
        MyComponent(text = "Long error message that should wrap properly")
    }
}
```

### Device Previews

```kotlin
@DevicePreviews          // Phone portrait, phone landscape, tablet portrait, tablet landscape
@SystemUiPreview         // Shows with status bar and navigation bar
@CompletePreviews        // Light, dark, and tablet variations
```

**Usage:**
```kotlin
@DevicePreviews
@Composable
private fun MyScreenPreview_Devices() {
    NovaAssistantTheme {
        MyScreen()
    }
}
```

## Preview Parameter Providers

Located in `PreviewParameterProviders.kt`, these provide realistic test data for previews:

### Available Providers

1. **`AssistantStateProvider`** - All possible assistant states
   - Idle, Initializing, Listening, Processing, Stopping, Error (with variations)

2. **`SettingsProvider`** - Different settings configurations
   - Default, high sensitivity, low sensitivity, disabled

3. **`ServiceStatusProvider`** - Various service states
   - Not running, running, long-running with multiple detections

4. **`WakeWordEventProvider`** - Wake word detection events
   - High confidence, medium confidence, low confidence

5. **`MainUiStateProvider`** - Complete UI states for main screen
   - Idle, listening, processing, error, initializing, long-running

6. **`SensitivityProvider`** - Sensitivity slider values
   - 0.0, 0.3, 0.5, 0.7, 1.0

### Usage Example

```kotlin
@Preview(
    name = "All States",
    showBackground = true,
    group = "States"
)
@Composable
private fun MyComponentPreview(
    @PreviewParameter(AssistantStateProvider::class) state: AssistantState
) {
    NovaAssistantTheme {
        MyComponent(state = state)
    }
}
```

This single preview function generates **7 different previews** (one for each state in the provider).

## Preview Organization

Previews are organized into logical groups for easy navigation in Android Studio:

```kotlin
// ========================================
// Previews
// ========================================

/**
 * Preview with all states using parameter provider.
 */
@Preview(
    name = "All States",
    showBackground = true,
    group = "States"          // Groups previews together
)

/**
 * Theme variations for the most important state.
 */
@ThemePreviews

/**
 * Accessibility testing.
 */
@AccessibilityPreviews
```

## Best Practices

### 1. Always Wrap in Theme

Every preview should be wrapped in `NovaAssistantTheme`:

```kotlin
@LightPreview
@Composable
private fun MyComponentPreview() {
    NovaAssistantTheme {  // ← Always include theme
        MyComponent()
    }
}
```

### 2. Use Descriptive Names

Preview function names should clearly indicate what they preview:

```kotlin
// Good ✅
@Composable
private fun StatusIndicatorPreview_Listening()

@Composable
private fun MainScreenPreview_WithError()

// Bad ❌
@Composable
private fun Preview1()

@Composable
private fun Test()
```

### 3. Include Documentation

Add KDoc comments explaining what the preview demonstrates:

```kotlin
/**
 * Shows the error card with a long message to test text wrapping
 * and accessibility with large fonts.
 */
@AccessibilityPreviews
@Composable
private fun ErrorCardPreview_LongMessage() {
    // ...
}
```

### 4. Choose Appropriate Preview Types

| Component Type | Recommended Previews |
|---------------|---------------------|
| Simple component (button, text) | `@LightPreview` or `@ThemePreviews` |
| Complex component (card, list item) | `@ThemePreviews` + `@AccessibilityPreviews` |
| Full screen | `@CompletePreviews` + `@SystemUiPreview` |
| Critical user flow | All preview types |

### 5. Test Edge Cases

Include previews for edge cases:

```kotlin
// Empty state
@LightPreview
@Composable
private fun ServiceInfoCardPreview_NoDetections()

// Long text
@AccessibilityPreviews
@Composable
private fun ErrorCardPreview_VeryLongMessage()

// Maximum values
@LightPreview
@Composable
private fun SensitivityControlPreview_Maximum()
```

## Current Preview Coverage

### Components with Full Preview Coverage ✅

- ✅ **StatusIndicator** - All states, themes, accessibility
- ✅ **MainScreen** - All UI states, themes, devices, accessibility
- ✅ **PermissionHandler** - Themes, accessibility, devices
- ✅ **WakeWordFeedback** - Themes, devices, with overlay
- ✅ **ServiceInfoCard** - Themes
- ✅ **SensitivityControl** - All values, enabled/disabled states
- ✅ **ToggleServiceButton** - All states (start, stop, loading, disabled), themes
- ✅ **ErrorCard** - Themes, accessibility with long messages
- ✅ **PermissionErrorCard** - Themes

## Viewing Previews in Android Studio

### Method 1: Split Editor
1. Open any `.kt` file with previews
2. Click the **Split** icon in the top-right
3. Choose "Design" view
4. See all previews rendered in real-time

### Method 2: Design Panel
1. Open any `.kt` file with previews
2. Switch to "Design" tab at the top
3. Use the dropdown to filter by preview group

### Method 3: Interactive Preview
1. Click on any preview
2. Click "Interactive" mode
3. Interact with buttons, sliders, etc.
4. Test animations in real-time

## Adding Previews to New Components

### Step-by-Step Guide

1. **Create your composable component:**
```kotlin
@Composable
fun MyNewComponent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Component implementation
}
```

2. **Add a basic preview:**
```kotlin
@LightPreview
@Composable
private fun MyNewComponentPreview() {
    NovaAssistantTheme {
        MyNewComponent(
            text = "Sample text",
            onClick = {}
        )
    }
}
```

3. **Add theme variations:**
```kotlin
@ThemePreviews
@Composable
private fun MyNewComponentPreview_Themes() {
    NovaAssistantTheme {
        MyNewComponent(
            text = "Sample text",
            onClick = {}
        )
    }
}
```

4. **Add parameter provider (if applicable):**
```kotlin
@Preview(name = "All States", showBackground = true)
@Composable
private fun MyNewComponentPreview_AllStates(
    @PreviewParameter(MyStateProvider::class) state: MyState
) {
    NovaAssistantTheme {
        MyNewComponent(state = state, onClick = {})
    }
}
```

5. **Add accessibility preview:**
```kotlin
@AccessibilityPreviews
@Composable
private fun MyNewComponentPreview_Accessibility() {
    NovaAssistantTheme {
        MyNewComponent(
            text = "Very long text that should wrap properly with large fonts",
            onClick = {}
        )
    }
}
```

## Performance Tips

1. **Use `showBackground = true`** - Shows component clearly
2. **Set `group` parameter** - Organizes previews in Android Studio
3. **Limit preview complexity** - Keep previews simple for fast rendering
4. **Use `remember { }`** - For state that doesn't need to change
5. **Avoid expensive operations** - No network calls, heavy computations

## Troubleshooting

### Previews Not Showing

**Problem:** Previews don't render in Android Studio
**Solutions:**
- Click "Build & Refresh" in the preview panel
- Invalidate caches: File > Invalidate Caches > Invalidate and Restart
- Ensure function is `private` and annotated with `@Preview` or multi-preview annotation
- Check that function is `@Composable`
- Verify function has no parameters (except `@PreviewParameter`)

### Slow Preview Rendering

**Problem:** Previews take too long to render
**Solutions:**
- Reduce number of previews per file
- Use `@Preview` annotation with specific configurations instead of multi-preview for less critical components
- Disable "Live Edit" mode
- Use preview groups to load only needed previews

### Preview Crashes

**Problem:** Preview throws exception or shows error
**Solutions:**
- Ensure all `@PreviewParameter` providers return valid data
- Wrap content in `NovaAssistantTheme` for proper theming
- Avoid using `hiltViewModel()` in preview functions (use fake data instead)
- Check for null safety issues

## Examples from Nova Assistant

### Example 1: Simple Component with Multiple States

```kotlin
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
```

This generates 7 previews showing all assistant states.

### Example 2: Complex Screen with Multiple Variations

```kotlin
@CompletePreviews  // Light, Dark, Tablet
@Composable
private fun MainScreenPreview_Listening_Complete() {
    NovaAssistantTheme {
        MainContent(
            uiState = MainUiState(
                assistantState = AssistantState.Listening,
                serviceStatus = ServiceStatus(isRunning = true),
                settings = Settings.default()
            ),
            wakeWordEvent = null,
            permissionsGranted = true,
            showPermissionError = false,
            onToggleService = {},
            onSensitivityChange = {},
            onClearError = {}
        )
    }
}
```

This generates 3 previews (light theme, dark theme, tablet).

### Example 3: Accessibility Testing

```kotlin
@AccessibilityPreviews  // Large font in light + dark
@Composable
private fun ErrorCardPreview_LongMessage() {
    NovaAssistantTheme {
        ErrorCard(
            message = "Wake word detection failed. The microphone permission was denied. " +
                     "Please go to Settings > Apps > Nova Assistant > Permissions.",
            onDismiss = {}
        )
    }
}
```

This generates 2 previews testing large font rendering in both themes.

## Resources

- [Android Compose Preview Documentation](https://developer.android.com/jetpack/compose/tooling/previews)
- [Preview Parameter Providers](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
- [Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)
- [Material 3 Design System](https://m3.material.io/)

## Contributing

When adding new UI components:
1. Add at least `@ThemePreviews` for the component
2. Add `@AccessibilityPreviews` if component displays text
3. Create a parameter provider if component has multiple states
4. Document edge cases with specific previews
5. Update this README if you create new multi-preview annotations

---

**Last Updated:** January 2026
**Maintained By:** Nova Assistant Team
