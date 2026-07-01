package com.voicebudget.domain.advisor.calculators

import java.time.YearMonth
import javax.inject.Inject

class IncomeTrendAnalyzer @Inject constructor() {

    /**
     * Returns true when income has declined in every consecutive pair within
     * the last [months] months (including the current month).
     * Requires at least two months of data to produce a result.
     */
    fun isIncomeDeclinig(
        incomeByMonth: Map<YearMonth, Double>,
        currentMonth: YearMonth,
        months: Int,
    ): Boolean {
        val values = (0 until months)
            .map { offset -> incomeByMonth[currentMonth.minusMonths(offset.toLong())] ?: 0.0 }
            .reversed()

        if (values.size < 2) return false
        return values.zipWithNext().all { (prev, curr) -> curr < prev }
    }

    /**
     * Returns the percentage change in income from [months] months ago to the current month,
     * or null when baseline income is zero.
     */
    fun overallChangePercent(
        incomeByMonth: Map<YearMonth, Double>,
        currentMonth: YearMonth,
        months: Int,
    ): Double? {
        val baseline = incomeByMonth[currentMonth.minusMonths(months.toLong() - 1)] ?: return null
        if (baseline == 0.0) return null
        val current = incomeByMonth[currentMonth] ?: 0.0
        return ((current - baseline) / baseline) * 100.0
    }
}
