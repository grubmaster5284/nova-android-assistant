package com.ctrlbsketr.novaassistant.util

import android.util.Log

/**
 * Centralized logging utility for wake word detection debugging.
 * Provides structured logging with consistent tags and log levels.
 *
 * All logs use the tag "NovaWakeWord" for easy filtering in logcat:
 * adb logcat -s NovaWakeWord
 */
object WakeWordLogger {
    private const val TAG = "NovaWakeWord"

    // Log levels
    fun d(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.d(TAG, message, throwable)
        } else {
            Log.d(TAG, message)
        }
    }

    fun i(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.i(TAG, message, throwable)
        } else {
            Log.i(TAG, message)
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(TAG, message, throwable)
        } else {
            Log.w(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }

    // Structured logging helpers
    fun logServiceLifecycle(action: String, details: String = "") {
        i("🔵 SERVICE: $action${if (details.isNotEmpty()) " - $details" else ""}")
    }

    fun logPorcupineInit(step: String, details: String = "") {
        i("🟢 PORCUPINE_INIT: $step${if (details.isNotEmpty()) " - $details" else ""}")
    }

    fun logPorcupineError(operation: String, error: Throwable) {
        e("🔴 PORCUPINE_ERROR: $operation", error)
    }

    fun logWakeWordDetected(keyword: String, confidence: Float) {
        i("✅ WAKE_WORD_DETECTED: keyword='$keyword', confidence=$confidence")
    }

    fun logAudioProcessing(operation: String, details: String = "") {
        d("🎤 AUDIO: $operation${if (details.isNotEmpty()) " - $details" else ""}")
    }

    fun logPermissionCheck(permission: String, granted: Boolean) {
        val status = if (granted) "GRANTED" else "DENIED"
        w("🔐 PERMISSION: $permission = $status")
    }

    fun logStateChange(from: String, to: String) {
        i("🔄 STATE: $from -> $to")
    }
}

