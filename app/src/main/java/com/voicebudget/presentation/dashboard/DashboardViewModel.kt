package com.voicebudget.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        getMonthlySummaryUseCase(),
        getTransactionsUseCase().map { it.take(RECENT_TRANSACTIONS_LIMIT) },
        observeSettingsUseCase(),
    ) { summary, recent, settings ->
        DashboardUiState(
            isLoading = false,
            summary = summary,
            recentTransactions = recent,
            currencySymbol = settings.currency.symbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
