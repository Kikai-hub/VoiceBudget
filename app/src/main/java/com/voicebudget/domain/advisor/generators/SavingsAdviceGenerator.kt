package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.advisor.calculators.SavingsCalculator
import javax.inject.Inject
import kotlin.math.roundToInt

private const val POSITIVE_THRESHOLD_PERCENT = 20.0

class SavingsAdviceGenerator @Inject constructor(
    private val incomeCalculator: MonthlyIncomeCalculator,
    private val expenseCalculator: MonthlyExpenseCalculator,
    private val savingsCalculator: SavingsCalculator,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val income = incomeCalculator.forMonth(context.allTransactions, context.currentMonth)
        val expenses = expenseCalculator.forMonth(context.allTransactions, context.currentMonth)
        val rate = savingsCalculator.savingsRatePercent(income, expenses) ?: return emptyList()

        return when {
            rate < context.settings.desiredSavingsRatePercent -> listOf(lowSavingsAdvice(context, rate, income, expenses))
            rate >= POSITIVE_THRESHOLD_PERCENT -> listOf(positiveSavingsAdvice(context, rate))
            else -> emptyList()
        }
    }

    private fun lowSavingsAdvice(
        context: AnalysisContext,
        rate: Double,
        income: Double,
        expenses: Double,
    ): FinancialAdvice {
        val id = "savings_low_${context.currentMonth}"
        val desired = income * context.settings.desiredSavingsRatePercent / 100.0
        return FinancialAdvice(
            id = id,
            title = "Low savings rate",
            description = "Your savings rate is ${rate.roundToInt()}% — below your ${context.settings.desiredSavingsRatePercent.roundToInt()}% goal. " +
                "Try reducing expenses by ${(desired - (income - expenses)).toLong()}.",
            priority = AdvicePriority.HIGH,
            icon = AdviceIcon.SAVINGS,
            type = AdviceType.LOW_SAVINGS,
            potentialSavings = desired - (income - expenses),
            createdAt = System.currentTimeMillis(),
            dismissed = id in context.dismissedIds,
        )
    }

    private fun positiveSavingsAdvice(context: AnalysisContext, rate: Double): FinancialAdvice {
        val id = "savings_positive_${context.currentMonth}"
        return FinancialAdvice(
            id = id,
            title = "Great savings!",
            description = "You saved ${rate.roundToInt()}% of your income this month. Keep it up!",
            priority = AdvicePriority.LOW,
            icon = AdviceIcon.POSITIVE,
            type = AdviceType.POSITIVE_PROGRESS,
            potentialSavings = null,
            createdAt = System.currentTimeMillis(),
            dismissed = id in context.dismissedIds,
        )
    }
}
