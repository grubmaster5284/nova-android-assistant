package com.ctrlbsketr.novaassistant.presentation

/**
 * Defines available main screen UI implementations.
 * Allows easy switching between different UI versions for testing and development.
 */
enum class MainScreenImplementation(val displayName: String) {
    /**
     * Original implementation (currently disconnected)
     */
    ORIGINAL("Original"),

    /**
     * Beta implementation with voice orb and minimalist design
     */
    BETA("Beta - Voice Orb"),

    /**
     * Alternate implementation with modern orb animations and minimalist design
     */
    ALT("Alt - Modern Orb"),

    /**
     * Future implementation slots
     */
    V2("Version 2 (Coming Soon)"),
    V3("Version 3 (Coming Soon)");

    companion object {
        /**
         * Default implementation to use
         */
        val DEFAULT = BETA

        /**
         * Get implementation by name, defaults to DEFAULT if not found
         */
        fun fromName(name: String?): MainScreenImplementation {
            return values().find { it.name == name } ?: DEFAULT
        }
    }
}
