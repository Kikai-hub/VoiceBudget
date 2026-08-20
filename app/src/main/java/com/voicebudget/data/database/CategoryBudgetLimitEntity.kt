package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budget_limits")
data class CategoryBudgetLimitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String? = null,
    val customCategoryId: Long? = null,
    val monthlyLimit: Double,
    val walletId: Long,
    val lastNotifiedMonth: String? = null,
    val lastNotifiedThreshold: Int? = null,
)
