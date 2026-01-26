package com.ctrlbsketr.novaassistant.config

import com.ctrlbsketr.novaassistant.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized configuration and secrets management.
 * 
 * Provides abstraction over BuildConfig values that are injected from .env file.
 * This allows:
 * - Easy swapping of configuration sources (BuildConfig, remote config, etc.)
 * - Type-safe access to configuration values
 * - Clear separation of concerns
 * - Easy testing with mock implementations
 * 
 * Values are read from .env file during build time and injected into BuildConfig.
 * See build.gradle.kts for .env file parsing.
 * 
 * @property porcupineAccessKey Porcupine wake word detection access key
 */
@Singleton
class AppConfig @Inject constructor() {
    
    /**
     * Porcupine access key for wake word detection.
     * Get a free key from: https://console.picovoice.ai/
     * 
     * @throws IllegalStateException if access key is not configured
     */
    val porcupineAccessKey: String
        get() {
            val key = BuildConfig.PORCUPINE_ACCESS_KEY
            if (key.isBlank()) {
                throw IllegalStateException(
                    "Porcupine access key is not configured. " +
                    "Please set PORCUPINE_ACCESS_KEY in your .env file. " +
                    "Get a free key from: https://console.picovoice.ai/"
                )
            }
            return key
        }
    
    /**
     * Check if Porcupine access key is configured.
     */
    fun isPorcupineConfigured(): Boolean {
        return BuildConfig.PORCUPINE_ACCESS_KEY.isNotBlank()
    }
    
    /**
     * Get Porcupine access key safely (returns null if not configured).
     */
    fun getPorcupineAccessKeyOrNull(): String? {
        return BuildConfig.PORCUPINE_ACCESS_KEY.takeIf { it.isNotBlank() }
    }
}

