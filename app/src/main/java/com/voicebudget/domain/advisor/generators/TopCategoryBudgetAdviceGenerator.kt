package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.model.Category
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val REDUCTION_FACTOR = 0.30
private const val MONTHS_IN_YEAR = 12

/**
 * Finds the category that strains the budget most (share > configured threshold)
 * and suggests reducing it by 30%, showing the annual saving potential.
 */
class TopCategoryBudgetAdviceGenerator @Inject constructor(
    private val categoryAnalyzer: CategoryAnalyzer,
    private val expenseCalculator: MonthlyExpenseCalculator,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val totalExpenses = expenseCalculator.forMonth(context.allTransactions, context.currentMonth)
        if (totalExpenses == 0.0) return emptyList()

        val breakdown = categoryAnalyzer.breakdownForMonth(context.allTransactions, context.currentMonth)
        val top = breakdown.firstOrNull() ?: return emptyList()

        val sharePercent = (top.amount / totalExpenses) * 100.0
        if (sharePercent < context.settings.topCategoryThresholdPercent) return emptyList()

        val yearlySaving = top.amount * REDUCTION_FACTOR * MONTHS_IN_YEAR
        val id = "top_category_${top.category.name}_${context.currentMonth}"
        val priority = if (sharePercent >= 50.0) AdvicePriority.HIGH else AdvicePriority.MEDIUM

        return listOf(
            FinancialAdvice(
                id = id,
                title = "Biggest expense: ${categoryDisplayName(top.category)}",
                description = "${categoryDisplayName(top.category)} takes ${sharePercent.roundToInt()}% of your budget. " +
                    "Reducing it by ${(REDUCTION_FACTOR * 100).toInt()}% could save ~${yearlySaving.roundToLong()} per year.",
                priority = priority,
                icon = AdviceIcon.TRENDING_UP,
                type = AdviceType.CATEGORY_GROWTH,
                potentialSavings = yearlySaving,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            ),
        )
    }

    private fun categoryDisplayName(category: Category): String =
        category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
