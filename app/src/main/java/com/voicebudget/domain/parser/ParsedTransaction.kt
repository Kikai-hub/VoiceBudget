package com.voicebudget.domain.parser

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType

data class ParsedTransaction(
    val type: TransactionType,
    val category: Category,
    val amount: Double,
    val description: String,
)

enum class ParseFailureReason {
    EMPTY_INPUT,
    AMOUNT_NOT_FOUND,
}

sealed interface ParseResult {
    data class Success(val transaction: ParsedTransaction) : ParseResult
    data class Failure(val reason: ParseFailureReason) : ParseResult
}
