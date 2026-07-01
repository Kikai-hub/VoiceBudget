package com.voicebudget.presentation.advisor

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.FinancialAdvice

data class AdvisorUiState(
    val isLoading: Boolean = true,
    val advice: List<FinancialAdvice> = emptyList(),
    val settings: AdvisorSettings = AdvisorSettings(),
    val currencySymbol: String = "₽",
)
