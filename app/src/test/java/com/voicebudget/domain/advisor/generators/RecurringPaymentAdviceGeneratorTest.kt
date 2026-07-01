package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.RecurringPaymentDetector
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class RecurringPaymentAdviceGeneratorTest {

    private val generator = RecurringPaymentAdviceGenerator(RecurringPaymentDetector())

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, category: Category, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis(year, month),
    )

    private fun context(transactions: List<Transaction>) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = YearMonth.of(2024, 3),
        settings = AdvisorSettings(analysisPeriodMonths = 3),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates advice for recurring payment`() {
        val txs = listOf(
            expense(1000.0, Category.UTILITIES, 2024, 1),
            expense(1050.0, Category.UTILITIES, 2024, 2),
            expense(980.0, Category.UTILITIES, 2024, 3),
        )
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.RECURRING_EXPENSE, result[0].type)
    }

    @Test
    fun `returns empty for no recurring payments`() {
        val txs = listOf(expense(100.0, Category.FOOD, 2024, 3))
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty for empty transactions`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }
}
