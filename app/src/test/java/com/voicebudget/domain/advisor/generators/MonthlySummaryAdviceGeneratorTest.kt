package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
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

class MonthlySummaryAdviceGeneratorTest {

    private val generator = MonthlySummaryAdviceGenerator(
        MonthlyIncomeCalculator(),
        MonthlyExpenseCalculator(),
        CategoryAnalyzer(),
        SavingsCalculator(),
    )
    private val month = YearMonth.of(2024, 1)

    private fun epochMillis() = month.atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun income(amount: Double) = Transaction(amount = amount, type = TransactionType.INCOME, category = Category.SALARY, description = "", createdAt = epochMillis())
    private fun expense(amount: Double, category: Category = Category.FOOD) = Transaction(amount = amount, type = TransactionType.EXPENSE, category = category, description = "", createdAt = epochMillis())

    private fun context(transactions: List<Transaction>) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = month,
        settings = AdvisorSettings(),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates summary when there is activity`() {
        val txs = listOf(income(5000.0), expense(2000.0))
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.MONTHLY_SUMMARY, result[0].type)
        assertTrue(result[0].description.contains("5000"))
        assertTrue(result[0].description.contains("2000"))
    }

    @Test
    fun `returns empty when no activity`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }

    @Test
    fun `includes largest category in description`() {
        val txs = listOf(income(5000.0), expense(3000.0, Category.FOOD), expense(1000.0, Category.CAFE))
        val result = generator.generate(context(txs))
        assertTrue(result[0].description.contains("Food"))
    }
}
