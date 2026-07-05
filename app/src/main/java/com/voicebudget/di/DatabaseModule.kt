package com.voicebudget.di

import android.content.Context
import androidx.room.Room
import com.voicebudget.data.database.AppDatabase
import com.voicebudget.data.database.FinancialGoalDao
import com.voicebudget.data.database.MIGRATION_1_2
import com.voicebudget.data.database.TransactionDao
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideFinancialGoalDao(database: AppDatabase): FinancialGoalDao = database.financialGoalDao()
}
