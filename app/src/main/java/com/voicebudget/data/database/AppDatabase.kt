package com.voicebudget.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, FinancialGoalEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun financialGoalDao(): FinancialGoalDao

    companion object {
        const val DATABASE_NAME = "voicebudget.db"
    }
}
