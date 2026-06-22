package com.voicebudget.domain.model

import java.time.YearMonth

data class MonthlyTrendPoint(
    val yearMonth: YearMonth,
    val income: Double,
    val expense: Double,
)
