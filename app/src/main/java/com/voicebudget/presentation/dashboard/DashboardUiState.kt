package com.voicebudget.presentation.dashboard

import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.goals.GoalWithStrategy
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.MonthlySummary
import com.voicebudget.domain.model.Transaction

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val topAdvice: List<FinancialAdvice> = emptyList(),
    val goals: List<GoalWithStrategy> = emptyList(),
    val currencySymbol: String = "₽",
    val customCategories: List<CustomCategory> = emptyList(),
)
