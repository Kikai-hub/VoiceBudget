package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val createdAt: Long,
    val goalId: Long? = null,
    val customCategoryId: Long? = null,
    val walletId: Long = 0,
    val transferGroupId: String? = null,
    val transferDirection: String? = null,
)
