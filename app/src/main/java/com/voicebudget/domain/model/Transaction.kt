package com.voicebudget.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val description: String,
    val createdAt: Long,
    val goalId: Long? = null,
    val customCategoryId: Long? = null,
)

/**
 * True for the auto-generated expense transaction a goal "set aside" contribution creates.
 * These move money into savings rather than spend it, so spending/pace analysis (monthly
 * summary, advisor, goal strategy) must exclude them or they inflate expense totals.
 */
val Transaction.isGoalContribution: Boolean get() = goalId != null
