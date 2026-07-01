package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdviceGenerator
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.calculators.RecurringPaymentDetector
import com.voicebudget.domain.model.Category
import javax.inject.Inject
import kotlin.math.roundToLong

class RecurringPaymentAdviceGenerator @Inject constructor(
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
                title = "Recurring payment detected",
                description = "You have a recurring ${categoryDisplayName(payment.category)} payment " +
                    "of ~${payment.typicalAmount.roundToLong()} for ${payment.monthsSeen} consecutive months.",
                priority = AdvicePriority.LOW,
                icon = AdviceIcon.REPEAT,
                type = AdviceType.RECURRING_EXPENSE,
                potentialSavings = null,
                createdAt = System.currentTimeMillis(),
                dismissed = id in context.dismissedIds,
            )
        }
    }

    private fun categoryDisplayName(category: Category): String =
        category.name.replace('_', ' ').lowercase()
}
