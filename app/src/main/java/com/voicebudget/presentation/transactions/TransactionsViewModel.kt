package com.voicebudget.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.usecase.DeleteTransactionUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    getTransactionsUseCase: GetTransactionsUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val filters = MutableStateFlow(TransactionFilters())
    private val editingTransaction = MutableStateFlow<Transaction?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        getTransactionsUseCase(),
        filters,
        editingTransaction,
        observeSettingsUseCase(),
    ) { transactions, currentFilters, editing, settings ->
        val range = currentFilters.dateRange.toTimeRange()
        val filtered = transactions.filter { transaction ->
            (currentFilters.category == null || transaction.category == currentFilters.category) &&
                (range == null || transaction.createdAt in range)
        }
        TransactionsUiState(
            isLoading = false,
            transactions = filtered,
            filters = currentFilters,
            editingTransaction = editing,
            currencySymbol = settings.currency.symbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun setCategoryFilter(category: Category?) {
        filters.update { it.copy(category = category) }
    }

    fun setDateRangeFilter(range: DateRangeFilter) {
        filters.update { it.copy(dateRange = range) }
    }

    fun startEditing(transaction: Transaction) {
        editingTransaction.value = transaction
    }

    fun cancelEditing() {
        editingTransaction.value = null
    }

    fun saveEdit(updated: Transaction) {
        viewModelScope.launch {
            updateTransactionUseCase(updated)
            editingTransaction.value = null
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { deleteTransactionUseCase(transaction) }
    }
}
