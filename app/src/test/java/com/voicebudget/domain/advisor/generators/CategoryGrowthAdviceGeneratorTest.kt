package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class CategoryGrowthAdviceGeneratorTest {

    private val generator = CategoryGrowthAdviceGenerator(CategoryAnalyzer())

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, category: Category, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis(year, month),
    )

    private fun context(transactions: List<Transaction>) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = YearMonth.of(2024, 2),
        settings = AdvisorSettings(),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates advice when category grew by more than 30 percent`() {
        val txs = listOf(
            expense(1000.0, Category.CAFE, 2024, 1),
            expense(1400.0, Category.CAFE, 2024, 2),
        )
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.CATEGORY_GROWTH, result[0].type)
        assertTrue(result[0].description.contains("40%"))
    }

    @Test
    fun `returns empty when growth is below threshold`() {
        val txs = listOf(
            expense(1000.0, Category.CAFE, 2024, 1),
            expense(1100.0, Category.CAFE, 2024, 2),
        )
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty for empty transactions`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }

    @Test
    fun `skips categories with no previous month data`() {
        val txs = listOf(expense(1000.0, Category.CAFE, 2024, 2))
        assertTrue(generator.generate(context(txs)).isEmpty())
    }
}
