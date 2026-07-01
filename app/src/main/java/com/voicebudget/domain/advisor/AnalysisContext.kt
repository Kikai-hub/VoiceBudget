package com.voicebudget.domain.advisor

import com.voicebudget.domain.model.Transaction
import java.time.YearMonth

data class AnalysisContext(
    val allTransactions: List<Transaction>,
    val currentMonth: YearMonth,
    val settings: AdvisorSettings,
    val dismissedIds: Set<String>,
)
