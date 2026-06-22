package com.voicebudget.presentation.statistics

import com.voicebudget.domain.model.CategoryAmount
import com.voicebudget.domain.model.MonthlyTrendPoint

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val categoryBreakdown: List<CategoryAmount> = emptyList(),
    val monthlyTrend: List<MonthlyTrendPoint> = emptyList(),
    val currencySymbol: String = "₽",
)
