package com.voicebudget.presentation.goals

import com.voicebudget.domain.goals.GoalWithStrategy
import java.time.Month
import java.time.YearMonth

data class GoalsUiState(
    val isLoading: Boolean = true,
    val goals: List<GoalWithStrategy> = emptyList(),
    val currencySymbol: String = "₽",
)

sealed class GoalDialogState {
    data object Hidden : GoalDialogState()

    data class Editing(
        val name: String = "",
        val amountText: String = "",
        val month: Month = YearMonth.now().month,
        val year: Int = YearMonth.now().year,
        val error: String? = null,
    ) : GoalDialogState()
}
