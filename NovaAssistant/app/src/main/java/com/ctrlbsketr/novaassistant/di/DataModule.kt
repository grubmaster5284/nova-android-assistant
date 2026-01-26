package com.ctrlbsketr.novaassistant.di

import com.ctrlbsketr.novaassistant.data.repository.SettingsRepositoryImpl
import com.ctrlbsketr.novaassistant.data.repository.WakeWordRepositoryImpl
import com.ctrlbsketr.novaassistant.domain.repository.SettingsRepository
import com.ctrlbsketr.novaassistant.domain.repository.WakeWordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer dependencies.
 * Binds repository interfaces to their implementations.
 *
 * This module demonstrates:
 * - Dependency Inversion Principle: Domain depends on abstractions (interfaces)
 * - Liskov Substitution Principle: Implementations can be swapped without breaking code
 * - Open/Closed Principle: Can add new implementations without modifying existing code
 *
 * Using @Binds instead of @Provides:
 * - More efficient (generates less code)
 * - Clearer intent (binding interface to implementation)
 * - Better compile-time validation
 *
 * Note: This module and its functions are used by Hilt at compile time through code generation.
 * IDE warnings about "unused" are false positives - Hilt processes these annotations during build.
 */
@Suppress("unused") // Used by Hilt at compile time
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * Bind SettingsRepository interface to its implementation.
     * Enables easy swapping of implementations for testing or different storage backends.
     */
    @Suppress("unused") // Used by Hilt at compile time
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl
    ): SettingsRepository

    /**
     * Bind WakeWordRepository interface to its implementation.
     * Enables swapping wake word engines (Porcupine -> different engine) without domain layer changes.
     */
    @Suppress("unused") // Used by Hilt at compile time
    @Binds
    @Singleton
    abstract fun bindWakeWordRepository(
        implementation: WakeWordRepositoryImpl
    ): WakeWordRepository
}
