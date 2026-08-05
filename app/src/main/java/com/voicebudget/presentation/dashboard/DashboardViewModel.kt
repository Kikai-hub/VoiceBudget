package com.voicebudget.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.ContributeToGoalUseCase
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.GetGoalsWithStrategyUseCase
import com.voicebudget.domain.usecase.GetMonthlySummaryUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveCustomCategoriesUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECENT_TRANSACTIONS_LIMIT = 5
private const val TOP_ADVICE_LIMIT = 3

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    getFinancialAdviceUseCase: GetFinancialAdviceUseCase,
    getGoalsWithStrategyUseCase: GetGoalsWithStrategyUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
    observeCustomCategoriesUseCase: ObserveCustomCategoriesUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            getMonthlySummaryUseCase(),
            getTransactionsUseCase().map { it.take(RECENT_TRANSACTIONS_LIMIT) },
            getFinancialAdviceUseCase().map { advice ->
                advice.filterNot { it.dismissed }.take(TOP_ADVICE_LIMIT)
            },
            getGoalsWithStrategyUseCase(),
        ) { summary, recent, topAdvice, goals -> DashboardCore(summary, recent, topAdvice, goals) },
        observeSettingsUseCase(),
        observeCustomCategoriesUseCase(),
    ) { core, settings, customCategories ->
        DashboardUiState(
            isLoading = false,
            summary = core.summary,
            recentTransactions = core.recentTransactions,
            topAdvice = core.topAdvice,
            goals = core.goals,
            currencySymbol = settings.currency.symbol,
            customCategories = customCategories,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun contributeToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch { contributeToGoalUseCase(goalId, amount) }
    }
}

private data class DashboardCore(
    val summary: com.voicebudget.domain.model.MonthlySummary,
    val recentTransactions: List<com.voicebudget.domain.model.Transaction>,
    val topAdvice: List<com.voicebudget.domain.advisor.FinancialAdvice>,
    val goals: List<com.voicebudget.domain.goals.GoalWithStrategy>,
)
