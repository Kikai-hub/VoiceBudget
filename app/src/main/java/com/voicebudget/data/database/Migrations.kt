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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `financial_goals` ADD COLUMN `savedAmount` REAL NOT NULL DEFAULT 0.0",
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `goalId` INTEGER DEFAULT NULL",
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_categories` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`icon` TEXT NOT NULL, " +
                "`color` INTEGER NOT NULL)",
        )
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `customCategoryId` INTEGER DEFAULT NULL",
        )
    }
}
