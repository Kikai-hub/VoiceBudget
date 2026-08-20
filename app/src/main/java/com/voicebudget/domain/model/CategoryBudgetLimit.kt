package com.voicebudget.domain.model

data class CategoryBudgetLimit(
    val id: Long = 0,
    val category: Category? = null,
    val customCategoryId: Long? = null,
    val monthlyLimit: Double,
    val walletId: Long,
    val lastNotifiedMonth: String? = null,
    val lastNotifiedThreshold: Int? = null,
)

/** True if [transaction] counts against this limit: matches by custom category first, else by built-in category. */
fun CategoryBudgetLimit.matches(transaction: Transaction): Boolean =
    if (customCategoryId != null) {
        transaction.customCategoryId == customCategoryId
    } else {
        transaction.customCategoryId == null && transaction.category == category
    }
