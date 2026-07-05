package com.voicebudget.domain.advisor.generators

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.IncomeTrendAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

class IncomeTrendAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
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
                title = androidContext.getString(R.string.advice_income_trend_title),
                description = androidContext.getString(
                    R.string.advice_income_trend_desc,
                    months,
                    changePercent.roundToInt(),
                ),
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
