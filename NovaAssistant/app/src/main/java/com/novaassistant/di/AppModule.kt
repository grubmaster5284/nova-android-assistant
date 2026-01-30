package com.novaassistant.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * Follows Dependency Inversion Principle - provides abstractions, not implementations.
 *
 * @InstallIn(SingletonComponent::class) means these dependencies live as long as the application.
 *
 * This module demonstrates:
 * - Single Responsibility: Provides only app-level dependencies
 * - Dependency Injection: Centralizes dependency creation
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide application context.
     * Used throughout the app for Android-specific operations.
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context
}
