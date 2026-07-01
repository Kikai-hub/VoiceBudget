package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class SmallPurchaseAnalyzerTest {

    private val analyzer = SmallPurchaseAnalyzer()
    private val month = YearMonth.of(2024, 1)

    private fun epochMillis(): Long =
        month.atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = Category.FOOD, description = "", createdAt = epochMillis(),
    )

    private fun income(amount: Double) = Transaction(
        amount = amount, type = TransactionType.INCOME, category = Category.SALARY, description = "", createdAt = epochMillis(),
    )

    @Test
    fun `countForMonth counts purchases below threshold`() {
        val txs = listOf(expense(100.0), expense(400.0), expense(600.0), income(5000.0))
        assertEquals(2, analyzer.countForMonth(txs, month, threshold = 500.0))
    }

    @Test
    fun `countForMonth does not count exact threshold amount`() {
        val txs = listOf(expense(500.0))
        assertEquals(0, analyzer.countForMonth(txs, month, threshold = 500.0))
    }

    @Test
    fun `countForMonth returns zero for empty list`() {
        assertEquals(0, analyzer.countForMonth(emptyList(), month, threshold = 500.0))
    }

    @Test
    fun `countForMonth excludes income transactions`() {
        val txs = listOf(income(100.0))
        assertEquals(0, analyzer.countForMonth(txs, month, threshold = 500.0))
    }
}
