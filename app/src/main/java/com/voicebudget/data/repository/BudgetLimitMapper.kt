package com.voicebudget.data.repository

import com.voicebudget.data.database.CategoryBudgetLimitEntity
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.CategoryBudgetLimit

fun CategoryBudgetLimitEntity.toDomain(): CategoryBudgetLimit = CategoryBudgetLimit(
    id = id,
    category = category?.let { runCatching { Category.valueOf(it) }.getOrNull() },
    customCategoryId = customCategoryId,
    monthlyLimit = monthlyLimit,
    walletId = walletId,
    lastNotifiedMonth = lastNotifiedMonth,
    lastNotifiedThreshold = lastNotifiedThreshold,
)

fun CategoryBudgetLimit.toEntity(): CategoryBudgetLimitEntity = CategoryBudgetLimitEntity(
    id = id,
    category = category?.name,
    customCategoryId = customCategoryId,
    monthlyLimit = monthlyLimit,
    walletId = walletId,
    lastNotifiedMonth = lastNotifiedMonth,
    lastNotifiedThreshold = lastNotifiedThreshold,
)
