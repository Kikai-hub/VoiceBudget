package com.voicebudget.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.GetMonthlySummaryUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val RECENT_TRANSACTIONS_LIMIT = 5
private const val TOP_ADVICE_LIMIT = 3

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    getFinancialAdviceUseCase: GetFinancialAdviceUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        getMonthlySummaryUseCase(),
        getTransactionsUseCase().map { it.take(RECENT_TRANSACTIONS_LIMIT) },
        getFinancialAdviceUseCase().map { advice ->
            advice.filterNot { it.dismissed }.take(TOP_ADVICE_LIMIT)
        },
        observeSettingsUseCase(),
    ) { summary, recent, topAdvice, settings ->
        DashboardUiState(
            isLoading = false,
            summary = summary,
            recentTransactions = recent,
            topAdvice = topAdvice,
            currencySymbol = settings.currency.symbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
