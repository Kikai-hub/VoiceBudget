package com.voicebudget.domain.advisor

data class AdvisorSettings(
    val smallPurchaseThreshold: Double = 500.0,
    /** Category is flagged when its share of total monthly expenses exceeds this percent. */
    val topCategoryThresholdPercent: Double = 30.0,
    val desiredSavingsRatePercent: Double = 10.0,
    val analysisPeriodMonths: Int = 3,
)
