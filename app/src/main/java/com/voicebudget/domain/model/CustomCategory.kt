package com.voicebudget.domain.model

/** A user-defined category, layered on top of the built-in [Category] set. */
data class CustomCategory(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: CustomCategoryIcon,
    val color: Long,
)
