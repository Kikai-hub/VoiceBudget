package com.voicebudget.domain.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class InactivityReminderCalculatorTest {

    private val calculator = InactivityReminderCalculator()
    private val now = 1_000_000_000_000L

    @Test
    fun `does not remind when there is no transaction history yet`() {
        assertFalse(calculator.shouldRemind(lastTransactionAtMillis = null, nowMillis = now))
    }

    @Test
    fun `does not remind when the last transaction is under 3 days old`() {
        val lastTransaction = now - TimeUnit.DAYS.toMillis(2)
        assertFalse(calculator.shouldRemind(lastTransaction, now))
    }

    @Test
    fun `does not remind exactly at the boundary minus one millisecond`() {
        val lastTransaction = now - TimeUnit.DAYS.toMillis(3) + 1
        assertFalse(calculator.shouldRemind(lastTransaction, now))
    }

    @Test
    fun `reminds once the last transaction is at least 3 days old`() {
        val lastTransaction = now - TimeUnit.DAYS.toMillis(3)
        assertTrue(calculator.shouldRemind(lastTransaction, now))
    }

    @Test
    fun `reminds when the last transaction is well over 3 days old`() {
        val lastTransaction = now - TimeUnit.DAYS.toMillis(10)
        assertTrue(calculator.shouldRemind(lastTransaction, now))
    }
}
