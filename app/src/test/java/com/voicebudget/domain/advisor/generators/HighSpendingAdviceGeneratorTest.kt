package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class HighSpendingAdviceGeneratorTest {

    private val generator = HighSpendingAdviceGenerator(MonthlyExpenseCalculator())

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = Category.FOOD, description = "", createdAt = epochMillis(year, month),
    )

    private fun context(transactions: List<Transaction>, currentMonth: YearMonth = YearMonth.of(2024, 2)) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = currentMonth,
        settings = AdvisorSettings(),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates HIGH advice when spending increased by more than 20 percent`() {
        val txs = listOf(expense(1000.0, 2024, 1), expense(1300.0, 2024, 2))
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.HIGH_SPENDING, result[0].type)
        assertEquals(AdvicePriority.HIGH, result[0].priority)
    }

    @Test
    fun `generates CRITICAL advice when spending increased by more than 50 percent`() {
        val txs = listOf(expense(1000.0, 2024, 1), expense(1600.0, 2024, 2))
        val result = generator.generate(context(txs))
        assertEquals(AdvicePriority.CRITICAL, result[0].priority)
    }

    @Test
    fun `returns empty when increase is below threshold`() {
        val txs = listOf(expense(1000.0, 2024, 1), expense(1100.0, 2024, 2))
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty when no previous month data`() {
        val txs = listOf(expense(1000.0, 2024, 2))
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty for empty transaction list`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }

    @Test
    fun `marks dismissed advice correctly`() {
        val txs = listOf(expense(1000.0, 2024, 1), expense(1300.0, 2024, 2))
        val dismissedId = "high_spending_2024-02"
        val ctx = AnalysisContext(txs, YearMonth.of(2024, 2), AdvisorSettings(), setOf(dismissedId))
        val result = generator.generate(ctx)
        assertEquals(true, result[0].dismissed)
    }
}
