package com.voicebudget.data.csv

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCsvTest {

    @Test
    fun `round trips a simple transaction`() {
        val transaction = Transaction(
            amount = 850.0,
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            description = "Taxi",
            createdAt = 1_700_000_000_000L,
        )

        val csv = TransactionCsv.toCsv(listOf(transaction))
        val parsed = TransactionCsv.fromCsv(csv)

        assertEquals(1, parsed.size)
        assertEquals(transaction.amount, parsed[0].amount, 0.0)
        assertEquals(transaction.type, parsed[0].type)
        assertEquals(transaction.category, parsed[0].category)
        assertEquals(transaction.description, parsed[0].description)
        assertEquals(transaction.createdAt, parsed[0].createdAt)
    }

    @Test
    fun `round trips a description containing a comma and a quote`() {
        val transaction = Transaction(
            amount = 120000.0,
            type = TransactionType.INCOME,
            category = Category.SALARY,
            description = "Salary, \"April\" bonus",
            createdAt = 1_700_000_000_000L,
        )

        val csv = TransactionCsv.toCsv(listOf(transaction))
        val parsed = TransactionCsv.fromCsv(csv)

        assertEquals(1, parsed.size)
        assertEquals(transaction.description, parsed[0].description)
    }

    @Test
    fun `empty list produces only the header`() {
        val csv = TransactionCsv.toCsv(emptyList())
        assertEquals("Date,Type,Category,Description,Amount", csv)
        assertEquals(emptyList<Transaction>(), TransactionCsv.fromCsv(csv))
    }

    @Test
    fun `parses multiple rows in order`() {
        val transactions = listOf(
            Transaction(amount = 350.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = 1_700_000_000_000L),
            Transaction(amount = 45000.0, type = TransactionType.INCOME, category = Category.FREELANCE, description = "Freelance", createdAt = 1_700_100_000_000L),
        )

        val parsed = TransactionCsv.fromCsv(TransactionCsv.toCsv(transactions))

        assertEquals(transactions.map { it.description }, parsed.map { it.description })
    }
}
