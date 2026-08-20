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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wallets` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`currency` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`orderIndex` INTEGER NOT NULL DEFAULT 0)",
        )
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `walletId` INTEGER NOT NULL DEFAULT 1",
        )
        db.execSQL(
            "ALTER TABLE `financial_goals` ADD COLUMN `walletId` INTEGER NOT NULL DEFAULT 1",
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `transferGroupId` TEXT DEFAULT NULL",
        )
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `transferDirection` TEXT DEFAULT NULL",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exchange_rates` (" +
                "`currencyCode` TEXT NOT NULL, " +
                "`rateToUsd` REAL NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`currencyCode`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `category_budget_limits` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`category` TEXT DEFAULT NULL, " +
                "`customCategoryId` INTEGER DEFAULT NULL, " +
                "`monthlyLimit` REAL NOT NULL, " +
                "`walletId` INTEGER NOT NULL, " +
                "`lastNotifiedMonth` TEXT DEFAULT NULL, " +
                "`lastNotifiedThreshold` INTEGER DEFAULT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recurring_transactions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`customCategoryId` INTEGER DEFAULT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`walletId` INTEGER NOT NULL, " +
                "`frequency` TEXT NOT NULL, " +
                "`startDate` INTEGER NOT NULL, " +
                "`nextRunAt` INTEGER NOT NULL, " +
                "`endDate` INTEGER DEFAULT NULL, " +
                "`active` INTEGER NOT NULL DEFAULT 1)",
        )
    }
}
