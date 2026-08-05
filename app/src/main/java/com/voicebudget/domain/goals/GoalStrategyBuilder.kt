package com.voicebudget.domain.goals

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.categoryLabelRes
import com.voicebudget.domain.model.isGoalContribution
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val SUGGESTION_CUT_PERCENT = 25

/**
 * Projects whether a [FinancialGoal] is reachable at the user's current savings pace and
 * builds a localized strategy message — the domain-layer equivalent of an advice generator,
 * but scoped to a single user-defined goal instead of the general advisor feed.
 */
class GoalStrategyBuilder @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
    private val incomeCalculator: MonthlyIncomeCalculator,
    private val expenseCalculator: MonthlyExpenseCalculator,
    private val categoryAnalyzer: CategoryAnalyzer,
) {
    fun build(goal: FinancialGoal, allTransactions: List<Transaction>): GoalStrategy {
        val now = YearMonth.now()
        val monthsRemaining = ChronoUnit.MONTHS.between(now, goal.targetMonth).toInt().coerceAtLeast(1)
        // Goal contributions are recorded as SAVINGS-category expenses (so the overall balance
        // reflects money set aside), but they must be excluded from the pace/suggestion math
        // here — otherwise "cut your top spending category" could suggest cutting savings itself.
        // Filtering by goalId (not category) also keeps a manually re-categorized contribution
        // correctly excluded, and avoids excluding unrelated transactions the user tagged SAVINGS
        // by hand outside the goal-contribution flow.
        val spendingTransactions = allTransactions.filterNot { it.isGoalContribution }
        val income = incomeCalculator.forMonth(spendingTransactions, now)
        val expenses = expenseCalculator.forMonth(spendingTransactions, now)
        val currentMonthlySavings = income - expenses
        val remainingAmount = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
        val requiredMonthlySavings = remainingAmount / monthsRemaining
        val progressPercent = if (goal.targetAmount > 0.0) {
            ((goal.savedAmount / goal.targetAmount) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            100
        }
        val goalReached = remainingAmount <= 0.0
        val onTrack = goalReached || currentMonthlySavings >= requiredMonthlySavings

        val monthYearLabel = monthYearLabel(goal.targetMonth)
        val message = when {
            goalReached -> androidContext.getString(R.string.goal_completed_desc, goal.name)
            onTrack -> androidContext.getString(
                R.string.goal_on_track_desc,
                goal.name,
                goal.targetAmount.roundToLong(),
                monthYearLabel,
                currentMonthlySavings.roundToLong(),
            )
            else -> androidContext.getString(
                R.string.goal_off_track_desc,
                goal.name,
                goal.targetAmount.roundToLong(),
                monthYearLabel,
                requiredMonthlySavings.roundToLong(),
                currentMonthlySavings.roundToLong(),
            )
        }

        val suggestion = if (onTrack) {
            null
        } else {
            categoryAnalyzer.topCategory(spendingTransactions, now)?.let { top ->
                val cutAmount = top.amount * SUGGESTION_CUT_PERCENT / 100.0
                androidContext.getString(
                    R.string.goal_suggestion_desc,
                    androidContext.getString(categoryLabelRes(top.category)),
                    SUGGESTION_CUT_PERCENT,
                    cutAmount.roundToLong(),
                )
            }
        }

        return GoalStrategy(
            monthsRemaining = monthsRemaining,
            savedAmount = goal.savedAmount,
            progressPercent = progressPercent,
            requiredMonthlySavings = requiredMonthlySavings,
            currentMonthlySavings = currentMonthlySavings,
            onTrack = onTrack,
            message = message,
            suggestion = suggestion,
        )
    }

    private fun monthYearLabel(month: YearMonth): String {
        val monthName = month.month.getDisplayName(TextStyle.FULL, java.util.Locale.getDefault())
        return "$monthName ${month.year}"
    }
}
