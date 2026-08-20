package com.voicebudget.di

import android.content.Context
import androidx.room.Room
import com.voicebudget.data.database.AppDatabase
import com.voicebudget.data.database.CategoryBudgetLimitDao
import com.voicebudget.data.database.CustomCategoryDao
import com.voicebudget.data.database.ExchangeRateDao
import com.voicebudget.data.database.FinancialGoalDao
import com.voicebudget.data.database.MIGRATION_1_2
import com.voicebudget.data.database.MIGRATION_2_3
import com.voicebudget.data.database.MIGRATION_3_4
import com.voicebudget.data.database.MIGRATION_4_5
import com.voicebudget.data.database.MIGRATION_5_6
import com.voicebudget.data.database.MIGRATION_6_7
import com.voicebudget.data.database.RecurringTransactionDao
import com.voicebudget.data.database.TransactionDao
import com.voicebudget.data.database.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )
            .build()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideFinancialGoalDao(database: AppDatabase): FinancialGoalDao = database.financialGoalDao()

    @Provides
    fun provideCustomCategoryDao(database: AppDatabase): CustomCategoryDao = database.customCategoryDao()

    @Provides
    fun provideWalletDao(database: AppDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideExchangeRateDao(database: AppDatabase): ExchangeRateDao = database.exchangeRateDao()

    @Provides
    fun provideCategoryBudgetLimitDao(database: AppDatabase): CategoryBudgetLimitDao =
        database.categoryBudgetLimitDao()

    @Provides
    fun provideRecurringTransactionDao(database: AppDatabase): RecurringTransactionDao =
        database.recurringTransactionDao()
}
