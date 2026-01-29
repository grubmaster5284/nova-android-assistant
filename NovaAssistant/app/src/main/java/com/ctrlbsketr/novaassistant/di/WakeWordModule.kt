package com.ctrlbsketr.novaassistant.di

import com.ctrlbsketr.novaassistant.features.wakeword.data.repository.WakeWordRepositoryImpl
import com.ctrlbsketr.novaassistant.features.wakeword.domain.repository.WakeWordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for Wake Word feature.
 * Following Clean Architecture - DI modules at root level.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WakeWordModule {

    @Binds
    @Singleton
    abstract fun bindWakeWordRepository(
        impl: WakeWordRepositoryImpl
    ): WakeWordRepository

    // Use cases use @Inject constructor and are auto-provided by Hilt
}
