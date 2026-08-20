package com.voicebudget.data.repository

import com.voicebudget.data.database.RecurringTransactionEntity
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.RecurrenceFrequency
import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.model.TransactionType

fun RecurringTransactionEntity.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    customCategoryId = customCategoryId,
    description = description,
    walletId = walletId,
    frequency = RecurrenceFrequency.valueOf(frequency),
    startDate = startDate,
    nextRunAt = nextRunAt,
    endDate = endDate,
    active = active,
)

fun RecurringTransaction.toEntity(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    customCategoryId = customCategoryId,
    description = description,
    walletId = walletId,
    frequency = frequency.name,
    startDate = startDate,
    nextRunAt = nextRunAt,
    endDate = endDate,
    active = active,
)
