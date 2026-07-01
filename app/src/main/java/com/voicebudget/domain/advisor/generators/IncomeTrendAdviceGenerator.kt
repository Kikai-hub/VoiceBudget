package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.IncomeTrendAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import javax.inject.Inject
import kotlin.math.roundToInt

class IncomeTrendAdviceGenerator @Inject constructor(
    private val incomeCalculator: MonthlyIncomeCalculator,
    private val trendAnalyzer: IncomeTrendAnalyzer,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val incomeByMonth = incomeCalculator.byMonth(context.allTransactions)
        val months = context.settings.analysisPeriodMonths

        if (!trendAnalyzer.isIncomeDeclinig(incomeByMonth, context.currentMonth, months)) {
            return emptyList()
        }

        val changePercent = trendAnalyzer.overallChangePercent(
            incomeByMonth,
            context.currentMonth,
            months,
        ) ?: return emptyList()

        val id = "income_trend_${context.currentMonth}"
        return listOf(
            FinancialAdvice(
                id = id,
                title = "Declining income",
                description = "Your income has been declining for $months consecutive months " +
                    "(${changePercent.roundToInt()}% change).",
                priority = AdvicePriority.HIGH,
                icon = AdviceIcon.INCOME,
                type = AdviceType.UNSTABLE_INCOME,
                potentialSavings = null,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            ),
        )
    }
}
