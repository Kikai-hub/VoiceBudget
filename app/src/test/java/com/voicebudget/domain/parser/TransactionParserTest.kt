package com.voicebudget.domain.parser

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionParserTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setUp() {
        parser = TransactionParser()
    }

    private fun parseSuccess(input: String): ParsedTransaction {
        val result = parser.parse(input)
        assertTrue("Expected Success but got $result", result is ParseResult.Success)
        return (result as ParseResult.Success).transaction
    }

    // --- English examples from the spec ---

    @Test
    fun `parses Coffee 350 as expense cafe`() {
        val transaction = parseSuccess("Coffee 350")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.CAFE, transaction.category)
        assertEquals(350.0, transaction.amount, 0.0)
        assertEquals("Coffee", transaction.description)
    }

    @Test
    fun `parses Taxi 850 as expense transport`() {
        val transaction = parseSuccess("Taxi 850")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.TRANSPORT, transaction.category)
        assertEquals(850.0, transaction.amount, 0.0)
        assertEquals("Taxi", transaction.description)
    }

    @Test
    fun `parses Groceries 2500 as expense food`() {
        val transaction = parseSuccess("Groceries 2500")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.FOOD, transaction.category)
        assertEquals(2500.0, transaction.amount, 0.0)
        assertEquals("Groceries", transaction.description)
    }

    @Test
    fun `parses Salary 120000 as income salary`() {
        val transaction = parseSuccess("Salary 120000")
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(Category.SALARY, transaction.category)
        assertEquals(120000.0, transaction.amount, 0.0)
        assertEquals("Salary", transaction.description)
    }

    @Test
    fun `parses space-grouped thousands as recognized by speech-to-text`() {
        val transaction = parseSuccess("Salary 2 000")
        assertEquals(2000.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses comma-grouped thousands as recognized by speech-to-text`() {
        val transaction = parseSuccess("Salary 2,000")
        assertEquals(2000.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses multi-group thousands`() {
        val transaction = parseSuccess("Salary 1,234,567")
        assertEquals(1234567.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses space-grouped millions as recognized by speech-to-text`() {
        val transaction = parseSuccess("Salary 2 000 000")
        assertEquals(2000000.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses non-breaking-space-grouped millions as recognized by ru-RU speech-to-text`() {
        val transaction = parseSuccess("Salary 2 000 000")
        assertEquals(2000000.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses decimal amount as kopecks not thousands`() {
        val transaction = parseSuccess("Coffee 99,50")
        assertEquals(99.5, transaction.amount, 0.0)
    }

    @Test
    fun `parses Freelance 45000 as income freelance`() {
        val transaction = parseSuccess("Freelance 45000")
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(Category.FREELANCE, transaction.category)
        assertEquals(45000.0, transaction.amount, 0.0)
        assertEquals("Freelance", transaction.description)
    }

    // --- Russian equivalents ---

    @Test
    fun `parses Kofe 350 as expense cafe`() {
        val transaction = parseSuccess("Кофе 350")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.CAFE, transaction.category)
        assertEquals(350.0, transaction.amount, 0.0)
        assertEquals("Кофе", transaction.description)
    }

    @Test
    fun `parses Taksi 850 rubley as expense transport`() {
        val transaction = parseSuccess("Такси 850 рублей")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.TRANSPORT, transaction.category)
        assertEquals(850.0, transaction.amount, 0.0)
        assertEquals("Такси", transaction.description)
    }

    @Test
    fun `parses Produkty 2500 as expense food`() {
        val transaction = parseSuccess("Продукты 2500")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.FOOD, transaction.category)
        assertEquals(2500.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses Zarplata 120000 as income salary`() {
        val transaction = parseSuccess("Зарплата 120000")
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(Category.SALARY, transaction.category)
        assertEquals(120000.0, transaction.amount, 0.0)
    }

    @Test
    fun `parses Frilans 45000 as income freelance`() {
        val transaction = parseSuccess("Фриланс 45000")
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(Category.FREELANCE, transaction.category)
        assertEquals(45000.0, transaction.amount, 0.0)
    }

    // --- Edge cases ---

    @Test
    fun `empty input fails with EMPTY_INPUT`() {
        val result = parser.parse("   ")
        assertEquals(ParseResult.Failure(ParseFailureReason.EMPTY_INPUT), result)
    }

    @Test
    fun `input without an amount fails with AMOUNT_NOT_FOUND`() {
        val result = parser.parse("Coffee")
        assertEquals(ParseResult.Failure(ParseFailureReason.AMOUNT_NOT_FOUND), result)
    }

    @Test
    fun `unknown keyword falls back to OTHER_EXPENSE`() {
        val transaction = parseSuccess("Xyzzy 500")
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(Category.OTHER_EXPENSE, transaction.category)
        assertEquals(500.0, transaction.amount, 0.0)
        assertEquals("Xyzzy", transaction.description)
    }

    @Test
    fun `decimal amount is parsed correctly`() {
        val transaction = parseSuccess("Coffee 350.50")
        assertEquals(350.50, transaction.amount, 0.0)
    }

    @Test
    fun `decimal amount with comma is parsed correctly`() {
        val transaction = parseSuccess("Кофе 350,50")
        assertEquals(350.50, transaction.amount, 0.0)
    }

    @Test
    fun `keyword order does not matter`() {
        val transaction = parseSuccess("850 такси")
        assertEquals(Category.TRANSPORT, transaction.category)
        assertEquals(850.0, transaction.amount, 0.0)
    }

    @Test
    fun `currency noise words are stripped from fallback description`() {
        val transaction = parseSuccess("500 rubles")
        assertEquals(Category.OTHER_EXPENSE, transaction.category)
        assertEquals(500.0, transaction.amount, 0.0)
        assertEquals("Other", transaction.description)
    }
}
