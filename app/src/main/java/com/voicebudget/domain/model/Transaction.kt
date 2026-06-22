package com.voicebudget.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val description: String,
    val createdAt: Long,
)
