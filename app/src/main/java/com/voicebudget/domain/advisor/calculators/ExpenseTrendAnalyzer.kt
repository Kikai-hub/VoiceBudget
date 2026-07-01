package com.voicebudget.domain.advisor.calculators

import java.time.YearMonth
import javax.inject.Inject

class ExpenseTrendAnalyzer @Inject constructor() {

    /**
     * Returns the percentage change between [current] and [previous] month expenses,
     * or null when [previous] is zero (no baseline to compare against).
     */
    fun percentChange(
        expensesByMonth: Map<YearMonth, Double>,
        current: YearMonth,
    ): Double? {
        val prev = expensesByMonth[current.minusMonths(1)] ?: return null
        if (prev == 0.0) return null
        val curr = expensesByMonth[current] ?: 0.0
        return ((curr - prev) / prev) * 100.0
    }
}
