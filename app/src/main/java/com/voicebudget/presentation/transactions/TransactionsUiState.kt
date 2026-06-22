package com.voicebudget.presentation.transactions

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction

enum class DateRangeFilter {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH,
    LAST_7_DAYS,
}

data class TransactionFilters(
    val category: Category? = null,
    val dateRange: DateRangeFilter = DateRangeFilter.ALL_TIME,
)

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val filters: TransactionFilters = TransactionFilters(),
    val editingTransaction: Transaction? = null,
    val currencySymbol: String = "₽",
)
