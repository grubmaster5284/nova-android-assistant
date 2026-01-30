package com.novaassistant.di

import android.content.Context
import com.novaassistant.features.audio.domain.manager.AudioDeviceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for Audio feature.
 * Following Clean Architecture - DI modules at root level.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioDeviceManager(
        @ApplicationContext context: Context
    ): AudioDeviceManager = AudioDeviceManager(context)

    // SoundPlayer is an object singleton - accessed directly via SoundPlayer.instance
    // No need to provide via DI
}
