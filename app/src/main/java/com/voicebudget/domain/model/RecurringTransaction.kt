package com.voicebudget.domain.model

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
}

data class RecurringTransaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val customCategoryId: Long? = null,
    val description: String,
    val walletId: Long,
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val nextRunAt: Long,
    val endDate: Long? = null,
    val active: Boolean = true,
)
