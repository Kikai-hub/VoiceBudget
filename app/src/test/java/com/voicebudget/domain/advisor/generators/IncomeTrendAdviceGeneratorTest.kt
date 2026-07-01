package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.IncomeTrendAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class IncomeTrendAdviceGeneratorTest {

    private val generator = IncomeTrendAdviceGenerator(MonthlyIncomeCalculator(), IncomeTrendAnalyzer())

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun income(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.INCOME, category = Category.SALARY, description = "", createdAt = epochMillis(year, month),
    )

    private fun context(transactions: List<Transaction>) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = YearMonth.of(2024, 3),
        settings = AdvisorSettings(analysisPeriodMonths = 3),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates advice when income declines for 3 months`() {
        val txs = listOf(
            income(3000.0, 2024, 1),
            income(2000.0, 2024, 2),
            income(1000.0, 2024, 3),
        )
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.UNSTABLE_INCOME, result[0].type)
    }

    @Test
    fun `returns empty when income is stable`() {
        val txs = listOf(
            income(2000.0, 2024, 1),
            income(2000.0, 2024, 2),
            income(2000.0, 2024, 3),
        )
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty for empty transactions`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }
}
