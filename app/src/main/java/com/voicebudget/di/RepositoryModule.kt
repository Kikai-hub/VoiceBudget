package com.voicebudget.di

import com.voicebudget.data.repository.BudgetLimitRepositoryImpl
import com.voicebudget.data.repository.CustomCategoryRepositoryImpl
import com.voicebudget.data.repository.ExchangeRateRepositoryImpl
import com.voicebudget.data.repository.GoalRepositoryImpl
import com.voicebudget.data.repository.RecurringTransactionRepositoryImpl
import com.voicebudget.data.repository.SecurityRepositoryImpl
import com.voicebudget.data.repository.SettingsRepositoryImpl
import com.voicebudget.data.repository.TransactionRepositoryImpl
import com.voicebudget.data.repository.TransactionRunnerImpl
import com.voicebudget.data.repository.WalletRepositoryImpl
import com.voicebudget.domain.repository.BudgetLimitRepository
import com.voicebudget.domain.repository.CustomCategoryRepository
import com.voicebudget.domain.repository.ExchangeRateRepository
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.RecurringTransactionRepository
import com.voicebudget.domain.repository.SecurityRepository
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
import com.voicebudget.domain.repository.WalletRepository
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

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(impl: ExchangeRateRepositoryImpl): ExchangeRateRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        impl: RecurringTransactionRepositoryImpl,
    ): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetLimitRepository(impl: BudgetLimitRepositoryImpl): BudgetLimitRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository
}
