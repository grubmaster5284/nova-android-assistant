package com.novaassistant.features.audio.domain.model

/**
 * Domain model for audio device information.
 * Pure Kotlin - NO Android imports.
 * Following Clean Architecture principles.
 */
data class AudioDevice(
    val type: DeviceType,
    val name: String,
    val isInput: Boolean,
    val isOutput: Boolean
)

/**
 * Supported audio device types.
 */
enum class DeviceType {
    WIRED_HEADSET,
    WIRED_HEADPHONES,
    BLUETOOTH,
    SPEAKER,
    EARPIECE,
    USB,
    UNKNOWN
}
