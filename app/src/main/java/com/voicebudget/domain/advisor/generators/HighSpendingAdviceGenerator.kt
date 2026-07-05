package com.voicebudget.domain.advisor.generators

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

private const val THRESHOLD_PERCENT = 20.0
private const val CRITICAL_THRESHOLD_PERCENT = 50.0

class HighSpendingAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
    private val expenseCalculator: MonthlyExpenseCalculator,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val current = expenseCalculator.forMonth(context.allTransactions, context.currentMonth)
        val previous = expenseCalculator.forMonth(
            context.allTransactions,
            context.currentMonth.minusMonths(1),
        )
        if (previous == 0.0) return emptyList()

        val changePercent = ((current - previous) / previous) * 100.0
        if (changePercent <= THRESHOLD_PERCENT) return emptyList()

        val id = "high_spending_${context.currentMonth}"
        val priority = if (changePercent >= CRITICAL_THRESHOLD_PERCENT) {
            AdvicePriority.CRITICAL
        } else {
            AdvicePriority.HIGH
        }

        return listOf(
            FinancialAdvice(
                id = id,
                title = androidContext.getString(R.string.advice_high_spending_title),
                description = androidContext.getString(
                    R.string.advice_high_spending_desc,
                    changePercent.roundToInt(),
                ),
                priority = priority,
                icon = AdviceIcon.TRENDING_UP,
                type = AdviceType.HIGH_SPENDING,
                potentialSavings = current - previous,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            ),
        )
    }
}
