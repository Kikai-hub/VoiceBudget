package com.voicebudget.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.GetCategoryBreakdownUseCase
import com.voicebudget.domain.usecase.GetMonthlyTrendUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    getMonthlyTrendUseCase: GetMonthlyTrendUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        getCategoryBreakdownUseCase(),
        getMonthlyTrendUseCase(),
        observeSettingsUseCase(),
    ) { breakdown, trend, settings ->
        StatisticsUiState(
            isLoading = false,
            categoryBreakdown = breakdown,
            monthlyTrend = trend,
            currencySymbol = settings.currency.symbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}
