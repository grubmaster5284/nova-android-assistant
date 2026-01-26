package com.ctrlbsketr.novaassistant.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctrlbsketr.novaassistant.presentation.main.MainScreen
import com.ctrlbsketr.novaassistant.presentation.main.MainViewModel
import com.ctrlbsketr.novaassistant.presentation.main_beta.BetaMainScreen
import com.ctrlbsketr.novaassistant.presentation.main_alt.AltMainScreen

/**
 * Router composable that switches between different main screen implementations.
 *
 * This allows for easy A/B testing and development of multiple UI versions
 * without affecting each other. Users can switch implementations via settings.
 *
 * @param onNavigateToSettings Callback to navigate to settings screen
 */
@Composable
fun MainScreenRouter(
    onNavigateToSettings: () -> Unit
) {
    // Get current implementation from settings
    val mainViewModel: MainViewModel = hiltViewModel()
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()

    val implementation = MainScreenImplementation.fromName(settings.uiImplementation)

    when (implementation) {
        MainScreenImplementation.ORIGINAL -> {
            // Fall back to Beta implementation
            BetaMainScreen(
                onNavigateToSettings = onNavigateToSettings
            )
        }

        MainScreenImplementation.BETA -> {
            BetaMainScreen(
                onNavigateToSettings = onNavigateToSettings
            )
        }

        MainScreenImplementation.ALT -> {
            AltMainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        MainScreenImplementation.V2 -> {
            // Fall back to Alt implementation
            AltMainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        MainScreenImplementation.V3 -> {
            // Fall back to Beta implementation
            BetaMainScreen(
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}
