package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.model.Category
import javax.inject.Inject
import kotlin.math.roundToInt

private const val GROWTH_THRESHOLD_PERCENT = 30.0

class CategoryGrowthAdviceGenerator @Inject constructor(
    private val categoryAnalyzer: CategoryAnalyzer,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val currentBreakdown = categoryAnalyzer.breakdownForMonth(
            context.allTransactions,
            context.currentMonth,
        ).associate { it.category to it.amount }

        val previousBreakdown = categoryAnalyzer.breakdownForMonth(
            context.allTransactions,
            context.currentMonth.minusMonths(1),
        ).associate { it.category to it.amount }

        return currentBreakdown.mapNotNull { (category, currentAmount) ->
            val prevAmount = previousBreakdown[category] ?: return@mapNotNull null
            if (prevAmount == 0.0) return@mapNotNull null

            val changePercent = ((currentAmount - prevAmount) / prevAmount) * 100.0
            if (changePercent <= GROWTH_THRESHOLD_PERCENT) return@mapNotNull null

            val id = "category_growth_${category.name}_${context.currentMonth}"
            FinancialAdvice(
                id = id,
                title = "${categoryDisplayName(category)} expenses grew",
                description = "${categoryDisplayName(category)} expenses increased by ${changePercent.roundToInt()}%.",
                priority = AdvicePriority.MEDIUM,
                icon = AdviceIcon.TRENDING_UP,
                type = AdviceType.CATEGORY_GROWTH,
                potentialSavings = currentAmount - prevAmount,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            )
        }
    }

    private fun categoryDisplayName(category: Category): String =
        category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
