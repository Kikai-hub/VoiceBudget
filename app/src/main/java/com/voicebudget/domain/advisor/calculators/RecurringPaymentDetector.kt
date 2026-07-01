package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.advisor.toYearMonth
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

data class RecurringPayment(
    val category: Category,
    val typicalAmount: Double,
    val monthsSeen: Int,
)

class RecurringPaymentDetector @Inject constructor() {

    private val amountToleranceFraction = 0.15

    /**
     * Finds transactions that appear in multiple consecutive months within the last
     * [lookbackMonths] months with a similar amount (+/-15% tolerance).
     */
    fun detect(
        transactions: List<Transaction>,
        currentMonth: YearMonth,
        lookbackMonths: Int = 3,
    ): List<RecurringPayment> {
        val months = (0 until lookbackMonths).map { currentMonth.minusMonths(it.toLong()) }.toSet()

        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.createdAt.toYearMonth() in months
        }

        return expenses
            .groupBy { it.category }
            .mapNotNull { (category, txs) -> detectRecurring(category, txs, months) }
    }

    private fun detectRecurring(
        category: Category,
        transactions: List<Transaction>,
        months: Set<YearMonth>,
    ): RecurringPayment? {
        val byMonth = transactions.groupBy { it.createdAt.toYearMonth() }
        val presentMonths = byMonth.keys.intersect(months)
        if (presentMonths.size < 2) return null

        // Use median amount of all transactions in this category as the reference
        val amounts = transactions.map { it.amount }.sorted()
        val median = amounts[amounts.size / 2]

        val similarMonths = presentMonths.filter { month ->
            byMonth[month]?.any { tx ->
                val diff = kotlin.math.abs(tx.amount - median) / median
                diff <= amountToleranceFraction
            } == true
        }

        if (similarMonths.size < 2) return null

        return RecurringPayment(
            category = category,
            typicalAmount = median,
            monthsSeen = similarMonths.size,
        )
    }
}
