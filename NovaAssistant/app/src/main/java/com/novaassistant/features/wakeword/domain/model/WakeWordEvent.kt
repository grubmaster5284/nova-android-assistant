package com.novaassistant.features.wakeword.domain.model

/**
 * Represents a wake word detection event.
 * Immutable data class following Value Object pattern.
 *
 * @param keyword The detected wake word keyword
 * @param confidence Confidence score (0.0 to 1.0)
 * @param timestamp Time when wake word was detected (milliseconds since epoch)
 */
data class WakeWordEvent(
    val keyword: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(confidence in 0.0f..1.0f) {
            "Confidence must be between 0.0 and 1.0"
        }
        require(keyword.isNotBlank()) {
            "Keyword cannot be blank"
        }
    }

    /**
     * Checks if confidence meets minimum threshold.
     */
    fun meetsThreshold(minimumConfidence: Float): Boolean {
        return confidence >= minimumConfidence
    }
}
