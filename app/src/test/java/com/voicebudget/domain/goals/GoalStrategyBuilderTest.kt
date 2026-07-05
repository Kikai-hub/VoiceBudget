package com.voicebudget.domain.goals

import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.fakes.fakeAndroidContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class GoalStrategyBuilderTest {

    private val builder = GoalStrategyBuilder(
        fakeAndroidContext(),
        MonthlyIncomeCalculator(),
        MonthlyExpenseCalculator(),
        CategoryAnalyzer(),
    )

    private fun epochMillisThisMonth(): Long =
        YearMonth.now().atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun income(amount: Double) = Transaction(
        amount = amount,
        type = TransactionType.INCOME,
        category = Category.SALARY,
        description = "",
        createdAt = epochMillisThisMonth(),
    )

    private fun expense(amount: Double, category: Category = Category.FOOD) = Transaction(
        amount = amount,
        type = TransactionType.EXPENSE,
        category = category,
        description = "",
        createdAt = epochMillisThisMonth(),
    )

    @Test
    fun `onTrack is true when current savings meet the required pace`() {
        val goal = FinancialGoal(
            name = "Vacation",
            targetAmount = 3000.0,
            targetMonth = YearMonth.now().plusMonths(3),
            createdAt = 0,
        )
        val txs = listOf(income(2000.0), expense(500.0))

        val strategy = builder.build(goal, txs)

        assertEquals(3, strategy.monthsRemaining)
        assertEquals(1000.0, strategy.requiredMonthlySavings, 0.001)
        assertEquals(1500.0, strategy.currentMonthlySavings, 0.001)
        assertTrue(strategy.onTrack)
        assertNull(strategy.suggestion)
    }

    @Test
    fun `onTrack is false and a category suggestion is included when savings fall short`() {
        val goal = FinancialGoal(
            name = "Car",
            targetAmount = 6000.0,
            targetMonth = YearMonth.now().plusMonths(3),
            createdAt = 0,
        )
        val txs = listOf(income(2000.0), expense(1800.0, Category.FOOD))

        val strategy = builder.build(goal, txs)

        assertFalse(strategy.onTrack)
        assertEquals(2000.0, strategy.requiredMonthlySavings, 0.001)
        assertEquals(200.0, strategy.currentMonthlySavings, 0.001)
        assertNotNull(strategy.suggestion)
        assertTrue(strategy.suggestion!!.contains("Food"))
    }

    @Test
    fun `monthsRemaining is clamped to at least 1 when the target month is now or past`() {
        val goal = FinancialGoal(
            name = "Urgent",
            targetAmount = 1000.0,
            targetMonth = YearMonth.now(),
            createdAt = 0,
        )

        val strategy = builder.build(goal, emptyList())

        assertEquals(1, strategy.monthsRemaining)
        assertEquals(1000.0, strategy.requiredMonthlySavings, 0.001)
    }
}
