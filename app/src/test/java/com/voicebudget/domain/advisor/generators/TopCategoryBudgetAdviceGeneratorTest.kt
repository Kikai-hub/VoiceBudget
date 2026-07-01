package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class TopCategoryBudgetAdviceGeneratorTest {

    private val generator = TopCategoryBudgetAdviceGenerator(CategoryAnalyzer(), MonthlyExpenseCalculator())
    private val month = YearMonth.of(2024, 1)

    private fun epochMillis() = month.atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, category: Category) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis(),
    )

    private fun context(transactions: List<Transaction>, threshold: Double = 30.0) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = month,
        settings = AdvisorSettings(topCategoryThresholdPercent = threshold),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates advice when top category exceeds threshold`() {
        val txs = listOf(
            expense(7000.0, Category.FOOD),   // 70% of total
            expense(3000.0, Category.TRANSPORT),
        )
        val result = generator.generate(context(txs, threshold = 30.0))
        assertEquals(1, result.size)
        assertEquals(AdviceType.CATEGORY_GROWTH, result[0].type)
        assertTrue(result[0].description.contains("70%"))
        assertTrue(result[0].description.contains("Food"))
    }

    @Test
    fun `generates HIGH priority when category takes more than 50 percent`() {
        val txs = listOf(
            expense(6000.0, Category.FOOD),
            expense(4000.0, Category.TRANSPORT),
        )
        val result = generator.generate(context(txs, threshold = 30.0))
        assertEquals(AdvicePriority.HIGH, result[0].priority)
    }

    @Test
    fun `generates MEDIUM priority when top category is between threshold and 50 percent`() {
        // FOOD is top at 40% (between 30% threshold and 50%)
        val txs = listOf(
            expense(4000.0, Category.FOOD),
            expense(2000.0, Category.TRANSPORT),
            expense(2000.0, Category.CAFE),
            expense(2000.0, Category.SHOPPING),
        )
        val result = generator.generate(context(txs, threshold = 30.0))
        assertEquals(AdvicePriority.MEDIUM, result[0].priority)
    }

    @Test
    fun `returns empty when all categories are below threshold`() {
        // Each category is 25% — below 30% threshold
        val txs = listOf(
            expense(2500.0, Category.FOOD),
            expense(2500.0, Category.TRANSPORT),
            expense(2500.0, Category.CAFE),
            expense(2500.0, Category.SHOPPING),
        )
        assertTrue(generator.generate(context(txs, threshold = 30.0)).isEmpty())
    }

    @Test
    fun `returns empty for empty transactions`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }

    @Test
    fun `potential savings is 30 percent of monthly amount times 12`() {
        val txs = listOf(
            expense(5000.0, Category.FOOD),
            expense(5000.0, Category.TRANSPORT),
        )
        val result = generator.generate(context(txs, threshold = 30.0))
        val expected = 5000.0 * 0.30 * 12
        assertEquals(expected, result[0].potentialSavings!!, 0.001)
    }
}
