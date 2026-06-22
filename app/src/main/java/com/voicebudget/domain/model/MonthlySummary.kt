package com.voicebudget.domain.model

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
) {
    val balance: Double get() = totalIncome - totalExpense
}
