package com.voicebudget.domain.goals

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.categoryLabelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import javax.inject.Inject
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
        val income = incomeCalculator.forMonth(allTransactions, now)
        val expenses = expenseCalculator.forMonth(allTransactions, now)
        val currentMonthlySavings = income - expenses
        val requiredMonthlySavings = goal.targetAmount / monthsRemaining
        val onTrack = currentMonthlySavings >= requiredMonthlySavings

        val monthYearLabel = monthYearLabel(goal.targetMonth)
        val message = if (onTrack) {
            androidContext.getString(
                R.string.goal_on_track_desc,
                goal.name,
                goal.targetAmount.roundToLong(),
                monthYearLabel,
                currentMonthlySavings.roundToLong(),
            )
        } else {
            androidContext.getString(
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
            categoryAnalyzer.topCategory(allTransactions, now)?.let { top ->
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
