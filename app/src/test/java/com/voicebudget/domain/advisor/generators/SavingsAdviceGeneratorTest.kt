package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.advisor.calculators.SavingsCalculator
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class SavingsAdviceGeneratorTest {

    private val generator = SavingsAdviceGenerator(
        MonthlyIncomeCalculator(),
        MonthlyExpenseCalculator(),
        SavingsCalculator(),
    )
    private val month = YearMonth.of(2024, 1)

    private fun epochMillis() = month.atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun income(amount: Double) = Transaction(amount = amount, type = TransactionType.INCOME, category = Category.SALARY, description = "", createdAt = epochMillis())
    private fun expense(amount: Double) = Transaction(amount = amount, type = TransactionType.EXPENSE, category = Category.FOOD, description = "", createdAt = epochMillis())

    private fun context(transactions: List<Transaction>, desiredRate: Double = 10.0) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = month,
        settings = AdvisorSettings(desiredSavingsRatePercent = desiredRate),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates LOW_SAVINGS advice when savings rate is below desired`() {
        val txs = listOf(income(10000.0), expense(9500.0))
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.LOW_SAVINGS, result[0].type)
        assertEquals(AdvicePriority.HIGH, result[0].priority)
    }

    @Test
    fun `generates POSITIVE_PROGRESS advice when savings rate is above 20 percent`() {
        val txs = listOf(income(10000.0), expense(7000.0))
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.POSITIVE_PROGRESS, result[0].type)
        assertEquals(AdvicePriority.LOW, result[0].priority)
    }

    @Test
    fun `returns empty when savings is between desired and 20 percent`() {
        val txs = listOf(income(10000.0), expense(8500.0))
        assertTrue(generator.generate(context(txs, desiredRate = 10.0)).isEmpty())
    }

    @Test
    fun `returns empty when no income`() {
        val txs = listOf(expense(1000.0))
        assertTrue(generator.generate(context(txs)).isEmpty())
    }
}
