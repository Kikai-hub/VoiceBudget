package com.voicebudget.domain.advisor.calculators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class ExpenseTrendAnalyzerTest {

    private val analyzer = ExpenseTrendAnalyzer()

    @Test
    fun `percentChange returns correct increase`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 1000.0,
            YearMonth.of(2024, 2) to 1270.0,
        )
        val result = analyzer.percentChange(data, YearMonth.of(2024, 2))
        assertEquals(27.0, result!!, 0.001)
    }

    @Test
    fun `percentChange returns negative for decrease`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 1000.0,
            YearMonth.of(2024, 2) to 800.0,
        )
        val result = analyzer.percentChange(data, YearMonth.of(2024, 2))
        assertEquals(-20.0, result!!, 0.001)
    }

    @Test
    fun `percentChange returns null when previous month missing`() {
        val data = mapOf(YearMonth.of(2024, 2) to 1000.0)
        assertNull(analyzer.percentChange(data, YearMonth.of(2024, 2)))
    }

    @Test
    fun `percentChange returns null when previous month is zero`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 0.0,
            YearMonth.of(2024, 2) to 500.0,
        )
        assertNull(analyzer.percentChange(data, YearMonth.of(2024, 2)))
    }
}
