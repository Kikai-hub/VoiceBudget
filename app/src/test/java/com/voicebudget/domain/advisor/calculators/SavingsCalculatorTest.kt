package com.voicebudget.domain.advisor.calculators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavingsCalculatorTest {

    private val calculator = SavingsCalculator()

    @Test
    fun `savingsRatePercent returns correct rate`() {
        val rate = calculator.savingsRatePercent(income = 10000.0, expenses = 8000.0)
        assertEquals(20.0, rate!!, 0.001)
    }

    @Test
    fun `savingsRatePercent returns null when income is zero`() {
        assertNull(calculator.savingsRatePercent(income = 0.0, expenses = 100.0))
    }

    @Test
    fun `savingsRatePercent handles expenses exceeding income`() {
        val rate = calculator.savingsRatePercent(income = 1000.0, expenses = 1200.0)
        assertEquals(-20.0, rate!!, 0.001)
    }

    @Test
    fun `savedAmount returns difference`() {
        assertEquals(2000.0, calculator.savedAmount(10000.0, 8000.0), 0.001)
    }

    @Test
    fun `savedAmount returns negative when overspending`() {
        assertEquals(-200.0, calculator.savedAmount(1000.0, 1200.0), 0.001)
    }
}
