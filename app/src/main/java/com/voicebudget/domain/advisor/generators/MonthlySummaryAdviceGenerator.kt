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
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.advisor.calculators.SavingsCalculator
import com.voicebudget.domain.model.categoryLabelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToLong

class MonthlySummaryAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
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
                title = androidContext.getString(R.string.advice_monthly_summary_title),
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
        append(androidContext.getString(R.string.advice_monthly_summary_base, income.roundToLong(), expenses.roundToLong()))
        topCategory?.let {
            append(" ")
            append(
                androidContext.getString(
                    R.string.advice_monthly_summary_top_category,
                    androidContext.getString(categoryLabelRes(it.category)),
                    it.amount.roundToLong(),
                ),
            )
        }
        savingsRate?.let {
            if (it < 10) {
                append(" ")
                append(androidContext.getString(R.string.advice_monthly_summary_save_more))
            }
        }
    }
}
