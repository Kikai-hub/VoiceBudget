package com.voicebudget.di

import com.voicebudget.data.repository.SettingsRepositoryImpl
import com.voicebudget.data.repository.TransactionRepositoryImpl
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
