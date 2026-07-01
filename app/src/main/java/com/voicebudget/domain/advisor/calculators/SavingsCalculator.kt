package com.voicebudget.domain.advisor.calculators

import javax.inject.Inject

class SavingsCalculator @Inject constructor() {

    /** Returns savings rate as a percentage [0..100+], or null when income is zero. */
    fun savingsRatePercent(income: Double, expenses: Double): Double? {
        if (income <= 0.0) return null
        return ((income - expenses) / income) * 100.0
    }

    fun savedAmount(income: Double, expenses: Double): Double = income - expenses
}
