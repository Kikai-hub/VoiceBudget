package com.voicebudget.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFormatterTest {

    @Test
    fun `whole numbers are formatted without decimals`() {
        assertEquals("350 ₽", formatAmount(350.0))
    }

    @Test
    fun `fractional amounts keep two decimal places`() {
        assertEquals("350.50 ₽", formatAmount(350.5))
    }

    @Test
    fun `large amounts are grouped with thousands separators`() {
        assertEquals("120,000 ₽", formatAmount(120000.0))
    }

    @Test
    fun `custom currency symbol is honored`() {
        assertEquals("100 $", formatAmount(100.0, "$"))
    }
}
