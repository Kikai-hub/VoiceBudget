package com.voicebudget.di

import com.voicebudget.data.repository.CustomCategoryRepositoryImpl
import com.voicebudget.data.repository.GoalRepositoryImpl
import com.voicebudget.data.repository.SettingsRepositoryImpl
import com.voicebudget.data.repository.TransactionRepositoryImpl
import com.voicebudget.data.repository.TransactionRunnerImpl
import com.voicebudget.domain.repository.CustomCategoryRepository
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
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

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindCustomCategoryRepository(impl: CustomCategoryRepositoryImpl): CustomCategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRunner(impl: TransactionRunnerImpl): TransactionRunner
}
