package com.novaassistant

import android.app.Application
import com.novaassistant.features.audio.domain.manager.SoundPlayer
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Nova Assistant.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 *
 * This triggers Hilt's code generation and creates:
 * - Application-level dependency container
 * - Component hierarchy for dependency injection
 * - Automatic injection into Android classes
 *
 * Key benefits:
 * - Centralized dependency management
 * - Compile-time dependency validation
 * - Automatic lifecycle management
 * - Easy testing with mock implementations
 */
@HiltAndroidApp
class NovaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize sound player for audio feedback
        SoundPlayer.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Release sound player resources
        SoundPlayer.release()
    }
}
