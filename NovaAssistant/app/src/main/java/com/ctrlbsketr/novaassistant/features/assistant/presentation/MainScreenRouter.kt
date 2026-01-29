package com.ctrlbsketr.novaassistant.features.assistant.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctrlbsketr.novaassistant.features.assistant.presentation.main.MainScreen
import com.ctrlbsketr.novaassistant.features.assistant.presentation.main.MainViewModel

/**
 * Router composable for the main screen.
 * Displays the modern orb UI implementation.
 *
 * @param onNavigateToSettings Callback to navigate to settings screen
 */
@Composable
fun MainScreenRouter(
    onNavigateToSettings: () -> Unit
) {
    val mainViewModel: MainViewModel = hiltViewModel()

    MainScreen(
        viewModel = mainViewModel,
        onNavigateToSettings = onNavigateToSettings
    )
}
