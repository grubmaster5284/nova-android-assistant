package com.ctrlbsketr.novaassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ctrlbsketr.novaassistant.features.wakeword.data.source.service.WakeWordService
import com.ctrlbsketr.novaassistant.features.assistant.presentation.MainScreenRouter
import com.ctrlbsketr.novaassistant.features.assistant.presentation.main.MainViewModel
import com.ctrlbsketr.novaassistant.features.settings.presentation.SettingsScreen
import com.ctrlbsketr.novaassistant.core.theme.NovaAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Nova Assistant.
 * Entry point for the application.
 *
 * Following official Porcupine demo pattern:
 * - Registers BroadcastReceiver for Porcupine initialization errors
 * - Passes errors to ViewModel for state management
 *
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 * Follows Single Activity architecture with Jetpack Compose navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var porcupineErrorReceiver: PorcupineErrorReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaAssistantTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreenRouter(
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register broadcast receiver for Porcupine errors
        // Following official demo pattern
        porcupineErrorReceiver = PorcupineErrorReceiver()
        val intentFilter = IntentFilter(WakeWordService.BROADCAST_PORCUPINE_ERROR)
        // Use ContextCompat for proper receiver flag handling on all Android versions
        ContextCompat.registerReceiver(
            this,
            porcupineErrorReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister broadcast receiver to prevent memory leaks
        porcupineErrorReceiver?.let {
            unregisterReceiver(it)
            porcupineErrorReceiver = null
        }
    }

    /**
     * BroadcastReceiver for Porcupine initialization errors.
     * Following official demo pattern - receives error broadcasts from WakeWordService.
     */
    private inner class PorcupineErrorReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val errorMessage = intent?.getStringExtra(WakeWordService.EXTRA_ERROR_MESSAGE)
                ?: "Unknown Porcupine error"
            viewModel.onPorcupineError(errorMessage)
        }
    }
}