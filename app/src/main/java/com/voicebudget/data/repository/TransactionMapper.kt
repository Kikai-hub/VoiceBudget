package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionEntity
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.model.TransferDirection

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    description = description,
    createdAt = createdAt,
    goalId = goalId,
    customCategoryId = customCategoryId,
    transferGroupId = transferGroupId,
    transferDirection = transferDirection?.let { runCatching { TransferDirection.valueOf(it) }.getOrNull() },
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    description = description,
    createdAt = createdAt,
    goalId = goalId,
    customCategoryId = customCategoryId,
    transferGroupId = transferGroupId,
    transferDirection = transferDirection?.name,
)
