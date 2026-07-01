package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class CategoryAnalyzerTest {

    private val analyzer = CategoryAnalyzer()

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, category: Category, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis(year, month),
    )

    @Test
    fun `breakdownForMonth groups and sums by category`() {
        val txs = listOf(
            expense(100.0, Category.CAFE, 2024, 1),
            expense(200.0, Category.CAFE, 2024, 1),
            expense(500.0, Category.FOOD, 2024, 1),
        )
        val result = analyzer.breakdownForMonth(txs, YearMonth.of(2024, 1))
        assertEquals(2, result.size)
        assertEquals(Category.FOOD, result[0].category)
        assertEquals(500.0, result[0].amount, 0.001)
        assertEquals(Category.CAFE, result[1].category)
        assertEquals(300.0, result[1].amount, 0.001)
    }

    @Test
    fun `breakdownForMonth returns empty for no transactions`() {
        assertEquals(true, analyzer.breakdownForMonth(emptyList(), YearMonth.of(2024, 1)).isEmpty())
    }

    @Test
    fun `topCategory returns highest spending category`() {
        val txs = listOf(
            expense(100.0, Category.CAFE, 2024, 1),
            expense(500.0, Category.FOOD, 2024, 1),
        )
        val top = analyzer.topCategory(txs, YearMonth.of(2024, 1))
        assertEquals(Category.FOOD, top?.category)
    }

    @Test
    fun `topCategory returns null for empty transactions`() {
        assertNull(analyzer.topCategory(emptyList(), YearMonth.of(2024, 1)))
    }

    @Test
    fun `amountForCategory sums only the given category`() {
        val txs = listOf(
            expense(200.0, Category.CAFE, 2024, 1),
            expense(300.0, Category.FOOD, 2024, 1),
        )
        assertEquals(200.0, analyzer.amountForCategory(txs, Category.CAFE, YearMonth.of(2024, 1)), 0.001)
        assertEquals(300.0, analyzer.amountForCategory(txs, Category.FOOD, YearMonth.of(2024, 1)), 0.001)
    }
}
