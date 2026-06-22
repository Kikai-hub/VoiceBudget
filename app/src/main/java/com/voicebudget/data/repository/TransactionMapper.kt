package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionEntity
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    description = description,
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    description = description,
    createdAt = createdAt,
)
