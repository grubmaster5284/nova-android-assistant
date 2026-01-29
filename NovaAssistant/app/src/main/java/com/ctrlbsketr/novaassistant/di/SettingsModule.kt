package com.ctrlbsketr.novaassistant.di

import android.content.Context
import com.ctrlbsketr.novaassistant.features.settings.data.repository.SettingsRepositoryImpl
import com.ctrlbsketr.novaassistant.features.settings.data.source.local.SettingsDataStore
import com.ctrlbsketr.novaassistant.features.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for Settings feature.
 * Following Clean Architecture - DI modules at root level.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(
            @ApplicationContext context: Context
        ): SettingsDataStore = SettingsDataStore(context)

        // Use cases use @Inject constructor and are auto-provided by Hilt
    }
}
