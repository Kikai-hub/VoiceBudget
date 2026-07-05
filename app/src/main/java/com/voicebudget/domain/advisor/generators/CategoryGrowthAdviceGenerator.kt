package com.voicebudget.domain.advisor.generators

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.model.categoryLabelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

private const val GROWTH_THRESHOLD_PERCENT = 30.0

class CategoryGrowthAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
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
            val displayName = androidContext.getString(categoryLabelRes(category))
            FinancialAdvice(
                id = id,
                title = androidContext.getString(R.string.advice_category_growth_title, displayName),
                description = androidContext.getString(
                    R.string.advice_category_growth_desc,
                    displayName,
                    changePercent.roundToInt(),
                ),
                priority = AdvicePriority.MEDIUM,
                icon = AdviceIcon.TRENDING_UP,
                type = AdviceType.CATEGORY_GROWTH,
                potentialSavings = currentAmount - prevAmount,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            )
        }
    }
}
