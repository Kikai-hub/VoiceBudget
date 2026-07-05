package com.voicebudget.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `financial_goals` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`targetAmount` REAL NOT NULL, " +
                "`targetYear` INTEGER NOT NULL, " +
                "`targetMonth` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)",
        )
    }
}
