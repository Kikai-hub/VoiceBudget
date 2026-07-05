package com.voicebudget.domain.advisor.generators

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.SmallPurchaseAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val COUNT_THRESHOLD = 20

class SmallPurchasesAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
    private val smallPurchaseAnalyzer: SmallPurchaseAnalyzer,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val count = smallPurchaseAnalyzer.countForMonth(
            context.allTransactions,
            context.currentMonth,
            context.settings.smallPurchaseThreshold,
        )
        if (count <= COUNT_THRESHOLD) return emptyList()

        val threshold = context.settings.smallPurchaseThreshold.toLong()
        val id = "small_purchases_${context.currentMonth}"

        return listOf(
            FinancialAdvice(
                id = id,
                title = androidContext.getString(R.string.advice_small_purchases_title),
                description = androidContext.getString(
                    R.string.advice_small_purchases_desc,
                    count,
                    threshold,
                ),
                priority = AdvicePriority.LOW,
                icon = AdviceIcon.SHOPPING,
                type = AdviceType.MANY_SMALL_PURCHASES,
                potentialSavings = null,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            ),
        )
    }
}
