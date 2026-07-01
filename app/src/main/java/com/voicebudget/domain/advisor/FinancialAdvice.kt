package com.voicebudget.domain.advisor

data class FinancialAdvice(
    val id: String,
    val title: String,
    val description: String,
    val priority: AdvicePriority,
    val icon: AdviceIcon,
    val type: AdviceType,
    val potentialSavings: Double?,
    val createdAt: Long,
    val dismissed: Boolean,
)
