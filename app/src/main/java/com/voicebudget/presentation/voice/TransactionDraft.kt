package com.voicebudget.presentation.voice

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.parser.ParsedTransaction

data class TransactionDraft(
    val amountText: String,
    val type: TransactionType,
    val category: Category,
    val description: String,
)

fun ParsedTransaction.toDraft(): TransactionDraft = TransactionDraft(
    amountText = amount.toPlainString(),
    type = type,
    category = category,
    description = description,
)

private fun Double.toPlainString(): String =
    if (this == Math.floor(this)) toLong().toString() else toString()
