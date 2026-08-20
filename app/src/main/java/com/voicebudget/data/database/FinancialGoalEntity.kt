package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val targetYear: Int,
    val targetMonth: Int,
    val createdAt: Long,
    val savedAmount: Double = 0.0,
    val walletId: Long = 0,
)
