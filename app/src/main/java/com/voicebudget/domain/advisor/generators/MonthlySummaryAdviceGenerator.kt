package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.advisor.calculators.SavingsCalculator
import com.voicebudget.domain.model.Category
import javax.inject.Inject
import kotlin.math.roundToLong

class MonthlySummaryAdviceGenerator @Inject constructor(
    private val incomeCalculator: MonthlyIncomeCalculator,
    private val expenseCalculator: MonthlyExpenseCalculator,
    private val categoryAnalyzer: CategoryAnalyzer,
    private val savingsCalculator: SavingsCalculator,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val income = incomeCalculator.forMonth(context.allTransactions, context.currentMonth)
        val expenses = expenseCalculator.forMonth(context.allTransactions, context.currentMonth)

        // Skip summary if no financial activity this month
        if (income == 0.0 && expenses == 0.0) return emptyList()

        val topCategory = categoryAnalyzer.topCategory(context.allTransactions, context.currentMonth)
        val savingsRate = savingsCalculator.savingsRatePercent(income, expenses)

        val id = "monthly_summary_${context.currentMonth}"
        val description = buildMonthlyDescription(income, expenses, topCategory, savingsRate)

        return listOf(
            FinancialAdvice(
                id = id,
                title = "Monthly summary",
                description = description,
                priority = AdvicePriority.LOW,
                icon = AdviceIcon.SUMMARY,
                type = AdviceType.MONTHLY_SUMMARY,
                potentialSavings = null,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            ),
        )
    }

    private fun buildMonthlyDescription(
        income: Double,
        expenses: Double,
        topCategory: com.voicebudget.domain.model.CategoryAmount?,
        savingsRate: Double?,
    ): String = buildString {
        append("Income: ${income.roundToLong()}, Expenses: ${expenses.roundToLong()}.")
        topCategory?.let {
            append(" Largest category: ${categoryDisplayName(it.category)} (${it.amount.roundToLong()}).")
        }
        savingsRate?.let {
            val opportunity = if (it < 10) " Try to save more next month." else ""
            append("$opportunity")
        }
    }

    private fun categoryDisplayName(category: Category): String =
        category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
