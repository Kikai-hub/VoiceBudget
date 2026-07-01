package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class MonthlyIncomeCalculatorTest {

    private val calculator = MonthlyIncomeCalculator()

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun income(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.INCOME, category = Category.SALARY, description = "", createdAt = epochMillis(year, month),
    )

    private fun expense(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount, type = TransactionType.EXPENSE, category = Category.FOOD, description = "", createdAt = epochMillis(year, month),
    )

    @Test
    fun `forMonth sums only income in target month`() {
        val txs = listOf(income(1000.0, 2024, 1), income(500.0, 2024, 1), expense(200.0, 2024, 1))
        assertEquals(1500.0, calculator.forMonth(txs, YearMonth.of(2024, 1)), 0.001)
    }

    @Test
    fun `forMonth returns zero for empty list`() {
        assertEquals(0.0, calculator.forMonth(emptyList(), YearMonth.of(2024, 1)), 0.001)
    }

    @Test
    fun `byMonth returns empty map for no income`() {
        val txs = listOf(expense(100.0, 2024, 1))
        assertEquals(true, calculator.byMonth(txs).isEmpty())
    }

    @Test
    fun `byMonth groups by month correctly`() {
        val txs = listOf(income(1000.0, 2024, 1), income(2000.0, 2024, 2))
        val result = calculator.byMonth(txs)
        assertEquals(1000.0, result[YearMonth.of(2024, 1)]!!, 0.001)
        assertEquals(2000.0, result[YearMonth.of(2024, 2)]!!, 0.001)
    }
}
