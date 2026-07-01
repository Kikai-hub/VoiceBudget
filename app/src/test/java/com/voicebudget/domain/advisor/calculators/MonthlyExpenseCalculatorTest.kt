package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class MonthlyExpenseCalculatorTest {

    private val calculator = MonthlyExpenseCalculator()

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month)
            .atDay(15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    private fun expense(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount,
        type = TransactionType.EXPENSE,
        category = Category.FOOD,
        description = "",
        createdAt = epochMillis(year, month),
    )

    private fun income(amount: Double, year: Int, month: Int) = Transaction(
        amount = amount,
        type = TransactionType.INCOME,
        category = Category.SALARY,
        description = "",
        createdAt = epochMillis(year, month),
    )

    @Test
    fun `forMonth sums only expenses in the target month`() {
        val transactions = listOf(
            expense(100.0, 2024, 1),
            expense(200.0, 2024, 1),
            expense(50.0, 2024, 2),
            income(1000.0, 2024, 1),
        )
        assertEquals(300.0, calculator.forMonth(transactions, YearMonth.of(2024, 1)), 0.001)
    }

    @Test
    fun `forMonth returns zero when no expenses in month`() {
        val transactions = listOf(expense(100.0, 2024, 2))
        assertEquals(0.0, calculator.forMonth(transactions, YearMonth.of(2024, 1)), 0.001)
    }

    @Test
    fun `forMonth returns zero for empty list`() {
        assertEquals(0.0, calculator.forMonth(emptyList(), YearMonth.of(2024, 1)), 0.001)
    }

    @Test
    fun `byMonth groups expenses correctly`() {
        val transactions = listOf(
            expense(100.0, 2024, 1),
            expense(200.0, 2024, 1),
            expense(400.0, 2024, 2),
        )
        val result = calculator.byMonth(transactions)
        assertEquals(300.0, result[YearMonth.of(2024, 1)]!!, 0.001)
        assertEquals(400.0, result[YearMonth.of(2024, 2)]!!, 0.001)
    }

    @Test
    fun `byMonth excludes income transactions`() {
        val transactions = listOf(income(5000.0, 2024, 1))
        val result = calculator.byMonth(transactions)
        assertEquals(true, result.isEmpty())
    }
}
