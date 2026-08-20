package com.voicebudget.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.RecurrenceFrequency
import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.CreateRecurringTransactionUseCase
import com.voicebudget.domain.usecase.DeleteRecurringTransactionUseCase
import com.voicebudget.domain.usecase.ObserveActiveWalletUseCase
import com.voicebudget.domain.usecase.ObserveCustomCategoriesUseCase
import com.voicebudget.domain.usecase.ObserveRecurringTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecurringCategoryOption {
    val type: TransactionType

    data class Builtin(val category: Category) : RecurringCategoryOption {
        override val type: TransactionType get() = category.type
    }

    data class Custom(val customCategory: CustomCategory) : RecurringCategoryOption {
        override val type: TransactionType get() = customCategory.type
    }
}

data class RecurringTransactionsUiState(
    val rules: List<RecurringTransaction> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val availableOptions: List<RecurringCategoryOption> = emptyList(),
    val activeWalletId: Long? = null,
)

sealed interface RecurringDialogState {
    data object Closed : RecurringDialogState
    data class Adding(
        val option: RecurringCategoryOption? = null,
        val amountText: String = "",
        val description: String = "",
        val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    ) : RecurringDialogState
}

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    observeRecurringTransactionsUseCase: ObserveRecurringTransactionsUseCase,
    observeCustomCategoriesUseCase: ObserveCustomCategoriesUseCase,
    observeActiveWalletUseCase: ObserveActiveWalletUseCase,
    private val createRecurringTransactionUseCase: CreateRecurringTransactionUseCase,
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<RecurringTransactionsUiState> = combine(
        observeRecurringTransactionsUseCase(),
        observeCustomCategoriesUseCase(),
        observeActiveWalletUseCase(),
    ) { rules, customCategories, activeWallet ->
        RecurringTransactionsUiState(
            rules = rules,
            customCategories = customCategories,
            availableOptions = Category.entries.map { RecurringCategoryOption.Builtin(it) } +
                customCategories.map { RecurringCategoryOption.Custom(it) },
            activeWalletId = activeWallet?.id,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringTransactionsUiState())

    private val _dialogState = MutableStateFlow<RecurringDialogState>(RecurringDialogState.Closed)
    val dialogState: StateFlow<RecurringDialogState> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = RecurringDialogState.Adding()
    }

    fun updateDraft(transform: (RecurringDialogState.Adding) -> RecurringDialogState.Adding) {
        val current = _dialogState.value
        if (current is RecurringDialogState.Adding) _dialogState.value = transform(current)
    }

    fun dismissDialog() {
        _dialogState.value = RecurringDialogState.Closed
    }

    fun confirmAdd() {
        val state = _dialogState.value
        if (state !is RecurringDialogState.Adding) return
        val option = state.option ?: return
        val amount = state.amountText.toDoubleOrNull() ?: return
        if (amount <= 0) return
        val walletId = uiState.value.activeWalletId ?: return
        val now = System.currentTimeMillis()

        val rule = RecurringTransaction(
            amount = amount,
            type = option.type,
            category = (option as? RecurringCategoryOption.Builtin)?.category
                ?: Category.other(option.type),
            customCategoryId = (option as? RecurringCategoryOption.Custom)?.customCategory?.id,
            description = state.description,
            walletId = walletId,
            frequency = state.frequency,
            startDate = now,
            nextRunAt = now,
        )
        viewModelScope.launch {
            createRecurringTransactionUseCase(rule)
            _dialogState.value = RecurringDialogState.Closed
        }
    }

    fun delete(rule: RecurringTransaction) {
        viewModelScope.launch { deleteRecurringTransactionUseCase(rule) }
    }
}
