package com.ctrlbsketr.novaassistant.features.assistant.presentation

/**
 * Defines available main screen UI implementations.
 * Reserved for future UI versions and A/B testing.
 */
enum class MainScreenImplementation(val displayName: String) {
    /**
     * Current primary implementation with modern orb animations
     */
    PRIMARY("Modern Orb"),

    /**
     * Future implementation slots
     */
    V2("Version 2 (Coming Soon)"),
    V3("Version 3 (Coming Soon)");

    companion object {
        /**
         * Default implementation to use
         */
        val DEFAULT = PRIMARY

        /**
         * Get implementation by name, defaults to DEFAULT if not found
         */
        fun fromName(name: String?): MainScreenImplementation {
            return values().find { it.name == name } ?: DEFAULT
        }
    }
}
