package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val customCategoryId: Long? = null,
    val description: String,
    val walletId: Long,
    val frequency: String,
    val startDate: Long,
    val nextRunAt: Long,
    val endDate: Long? = null,
    val active: Boolean = true,
)
