package com.voicebudget.domain.advisor.generators

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.calculators.SmallPurchaseAnalyzer
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class SmallPurchasesAdviceGeneratorTest {

    private val generator = SmallPurchasesAdviceGenerator(SmallPurchaseAnalyzer())
    private val month = YearMonth.of(2024, 1)

    private fun epochMillis() = month.atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun expense(amount: Double) = Transaction(amount = amount, type = TransactionType.EXPENSE, category = Category.FOOD, description = "", createdAt = epochMillis())

    private fun context(transactions: List<Transaction>, threshold: Double = 500.0) = AnalysisContext(
        allTransactions = transactions,
        currentMonth = month,
        settings = AdvisorSettings(smallPurchaseThreshold = threshold),
        dismissedIds = emptySet(),
    )

    @Test
    fun `generates advice when more than 20 small purchases`() {
        val txs = (1..25).map { expense(100.0) }
        val result = generator.generate(context(txs))
        assertEquals(1, result.size)
        assertEquals(AdviceType.MANY_SMALL_PURCHASES, result[0].type)
        assertTrue(result[0].description.contains("25"))
    }

    @Test
    fun `returns empty when count is 20 or fewer`() {
        val txs = (1..20).map { expense(100.0) }
        assertTrue(generator.generate(context(txs)).isEmpty())
    }

    @Test
    fun `returns empty for empty list`() {
        assertTrue(generator.generate(context(emptyList())).isEmpty())
    }
}
