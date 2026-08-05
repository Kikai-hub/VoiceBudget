package com.voicebudget.presentation.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.R
import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.usecase.AddGoalUseCase
import com.voicebudget.domain.usecase.ContributeToGoalUseCase
import com.voicebudget.domain.usecase.DeleteGoalUseCase
import com.voicebudget.domain.usecase.GetGoalsWithStrategyUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    getGoalsWithStrategyUseCase: GetGoalsWithStrategyUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val contributeToGoalUseCase: ContributeToGoalUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        getGoalsWithStrategyUseCase(),
        observeSettingsUseCase(),
    ) { goals, settings ->
        GoalsUiState(isLoading = false, goals = goals, currencySymbol = settings.currency.symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    private val _dialogState = MutableStateFlow<GoalDialogState>(GoalDialogState.Hidden)
    val dialogState: StateFlow<GoalDialogState> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = GoalDialogState.Editing()
    }

    fun updateDraft(transform: (GoalDialogState.Editing) -> GoalDialogState.Editing) {
        val current = _dialogState.value
        if (current is GoalDialogState.Editing) {
            _dialogState.value = transform(current)
        }
    }

    fun dismissDialog() {
        _dialogState.value = GoalDialogState.Hidden
    }

    fun confirmAdd() {
        val current = _dialogState.value
        if (current !is GoalDialogState.Editing) return

        if (current.name.isBlank()) {
            _dialogState.value = current.copy(error = context.getString(R.string.error_goal_invalid_name))
            return
        }
        val amount = current.amountText.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _dialogState.value = current.copy(error = context.getString(R.string.error_goal_invalid_amount))
            return
        }
        val targetMonth = YearMonth.of(current.year, current.month)
        if (targetMonth < YearMonth.now()) {
            _dialogState.value = current.copy(error = context.getString(R.string.error_goal_deadline_past))
            return
        }

        viewModelScope.launch {
            addGoalUseCase(
                FinancialGoal(
                    name = current.name,
                    targetAmount = amount,
                    targetMonth = targetMonth,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            _dialogState.value = GoalDialogState.Hidden
        }
    }

    fun deleteGoal(goal: FinancialGoal) {
        viewModelScope.launch { deleteGoalUseCase(goal) }
    }

    fun contributeToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch { contributeToGoalUseCase(goalId, amount) }
    }
}
