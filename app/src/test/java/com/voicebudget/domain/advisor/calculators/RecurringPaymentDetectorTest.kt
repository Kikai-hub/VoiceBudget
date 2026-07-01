package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class RecurringPaymentDetectorTest {

    private val detector = RecurringPaymentDetector()

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double, category: Category, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis(year, month),
    )

    @Test
    fun `detect finds recurring payment in 3 months`() {
        val txs = listOf(
            expense(1000.0, Category.UTILITIES, 2024, 1),
            expense(1050.0, Category.UTILITIES, 2024, 2),
            expense(980.0, Category.UTILITIES, 2024, 3),
        )
        val result = detector.detect(txs, YearMonth.of(2024, 3), lookbackMonths = 3)
        assertEquals(1, result.size)
        assertEquals(Category.UTILITIES, result[0].category)
    }

    @Test
    fun `detect returns empty when only one month present`() {
        val txs = listOf(expense(1000.0, Category.UTILITIES, 2024, 3))
        val result = detector.detect(txs, YearMonth.of(2024, 3), lookbackMonths = 3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detect returns empty for empty transaction list`() {
        val result = detector.detect(emptyList(), YearMonth.of(2024, 3), lookbackMonths = 3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detect ignores categories with wildly different amounts`() {
        val txs = listOf(
            expense(100.0, Category.CAFE, 2024, 1),
            expense(5000.0, Category.CAFE, 2024, 2),
        )
        val result = detector.detect(txs, YearMonth.of(2024, 2), lookbackMonths = 2)
        assertTrue(result.isEmpty())
    }
}
