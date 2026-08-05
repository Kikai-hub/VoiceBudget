package com.voicebudget.domain.goals

import java.time.YearMonth

data class FinancialGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val targetMonth: YearMonth,
    val createdAt: Long,
    val savedAmount: Double = 0.0,
)
