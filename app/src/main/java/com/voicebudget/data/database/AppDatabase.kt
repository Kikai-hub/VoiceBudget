package com.voicebudget.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        FinancialGoalEntity::class,
        CustomCategoryEntity::class,
        WalletEntity::class,
        ExchangeRateEntity::class,
        CategoryBudgetLimitEntity::class,
        RecurringTransactionEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun financialGoalDao(): FinancialGoalDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun categoryBudgetLimitDao(): CategoryBudgetLimitDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        const val DATABASE_NAME = "voicebudget.db"
    }
}
