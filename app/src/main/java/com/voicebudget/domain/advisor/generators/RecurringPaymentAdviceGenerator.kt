package com.voicebudget.domain.advisor.generators

import android.content.Context
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.RecurringPaymentDetector
import com.voicebudget.domain.model.categoryLabelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToLong

class RecurringPaymentAdviceGenerator @Inject constructor(
    @param:ApplicationContext private val androidContext: Context,
    private val detector: RecurringPaymentDetector,
) : AdviceGenerator {

    override fun generate(context: AnalysisContext): List<FinancialAdvice> {
        val recurring = detector.detect(
            context.allTransactions,
            context.currentMonth,
            context.settings.analysisPeriodMonths,
        )

        return recurring.map { payment ->
            val id = "recurring_${payment.category.name}_${context.currentMonth}"
            FinancialAdvice(
                id = id,
                title = androidContext.getString(R.string.advice_recurring_title),
                description = androidContext.getString(
                    R.string.advice_recurring_desc,
                    androidContext.getString(categoryLabelRes(payment.category)),
                    payment.typicalAmount.roundToLong(),
                    payment.monthsSeen,
                ),
                priority = AdvicePriority.LOW,
                icon = AdviceIcon.REPEAT,
                type = AdviceType.RECURRING_EXPENSE,
                potentialSavings = null,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            )
        }
    }
}
