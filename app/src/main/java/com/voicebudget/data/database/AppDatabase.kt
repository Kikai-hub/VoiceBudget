package com.voicebudget.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, FinancialGoalEntity::class, CustomCategoryEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun financialGoalDao(): FinancialGoalDao
    abstract fun customCategoryDao(): CustomCategoryDao

    companion object {
        const val DATABASE_NAME = "voicebudget.db"
    }
}
