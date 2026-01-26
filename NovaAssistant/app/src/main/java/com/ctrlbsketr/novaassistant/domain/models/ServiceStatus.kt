package com.ctrlbsketr.novaassistant.domain.models

/**
 * Represents the operational status of the wake word service.
 * Immutable data class for service state information.
 *
 * @param isRunning Whether the service is currently running
 * @param isListening Whether the service is actively listening
 * @param batteryImpact Estimated battery impact percentage per hour
 * @param detectionCount Number of wake words detected since service start
 * @param startTime Time when service was started (null if not running)
 */
data class ServiceStatus(
    val isRunning: Boolean = false,
    val isListening: Boolean = false,
    val batteryImpact: Float = 0f,
    val detectionCount: Int = 0,
    val startTime: Long? = null
) {
    /**
     * Calculate service uptime in milliseconds.
     * Returns null if service is not running.
     */
    fun getUptimeMillis(): Long? {
        return startTime?.let { System.currentTimeMillis() - it }
    }

    /**
     * Check if service has been running for a minimum duration.
     */
    fun hasBeenRunningFor(durationMillis: Long): Boolean {
        val uptime = getUptimeMillis() ?: return false
        return uptime >= durationMillis
    }
}
