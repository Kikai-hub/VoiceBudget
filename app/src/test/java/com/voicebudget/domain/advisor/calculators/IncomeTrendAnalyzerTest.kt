package com.voicebudget.domain.advisor.calculators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class IncomeTrendAnalyzerTest {

    private val analyzer = IncomeTrendAnalyzer()

    @Test
    fun `isIncomeDeclinig returns true for strictly declining income`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 3000.0,
            YearMonth.of(2024, 2) to 2000.0,
            YearMonth.of(2024, 3) to 1000.0,
        )
        assertEquals(true, analyzer.isIncomeDeclinig(data, YearMonth.of(2024, 3), 3))
    }

    @Test
    fun `isIncomeDeclinig returns false when income is stable`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 1000.0,
            YearMonth.of(2024, 2) to 1000.0,
            YearMonth.of(2024, 3) to 1000.0,
        )
        assertEquals(false, analyzer.isIncomeDeclinig(data, YearMonth.of(2024, 3), 3))
    }

    @Test
    fun `isIncomeDeclinig returns false when income partially recovers`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 3000.0,
            YearMonth.of(2024, 2) to 2000.0,
            YearMonth.of(2024, 3) to 2500.0,
        )
        assertEquals(false, analyzer.isIncomeDeclinig(data, YearMonth.of(2024, 3), 3))
    }

    @Test
    fun `overallChangePercent computes correct drop`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 3000.0,
            YearMonth.of(2024, 3) to 2100.0,
        )
        val result = analyzer.overallChangePercent(data, YearMonth.of(2024, 3), 3)
        assertEquals(-30.0, result!!, 0.001)
    }

    @Test
    fun `overallChangePercent returns null when baseline is zero`() {
        val data = mapOf(
            YearMonth.of(2024, 1) to 0.0,
            YearMonth.of(2024, 3) to 1000.0,
        )
        assertNull(analyzer.overallChangePercent(data, YearMonth.of(2024, 3), 3))
    }
}
