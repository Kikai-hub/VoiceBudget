package com.voicebudget.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.BudgetLimitProgress
import com.voicebudget.domain.usecase.DeleteBudgetLimitUseCase
import com.voicebudget.domain.usecase.GetBudgetLimitProgressUseCase
import com.voicebudget.domain.usecase.ObserveActiveWalletUseCase
import com.voicebudget.domain.usecase.ObserveCustomCategoriesUseCase
import com.voicebudget.domain.usecase.SetBudgetLimitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One selectable target for a budget limit: either a built-in category or a user-defined one. */
sealed interface BudgetCategoryOption {
    data class Builtin(val category: Category) : BudgetCategoryOption
    data class Custom(val customCategory: CustomCategory) : BudgetCategoryOption
}

data class BudgetLimitsUiState(
    val progress: List<BudgetLimitProgress> = emptyList(),
    val availableOptions: List<BudgetCategoryOption> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val activeWalletId: Long? = null,
)

sealed interface BudgetLimitDialogState {
    data object Closed : BudgetLimitDialogState
    data class Adding(val option: BudgetCategoryOption? = null, val amountText: String = "") : BudgetLimitDialogState
}

@HiltViewModel
class BudgetLimitsViewModel @Inject constructor(
    getBudgetLimitProgressUseCase: GetBudgetLimitProgressUseCase,
    observeCustomCategoriesUseCase: ObserveCustomCategoriesUseCase,
    observeActiveWalletUseCase: ObserveActiveWalletUseCase,
    private val setBudgetLimitUseCase: SetBudgetLimitUseCase,
    private val deleteBudgetLimitUseCase: DeleteBudgetLimitUseCase,
) : ViewModel() {

    val uiState: StateFlow<BudgetLimitsUiState> = combine(
        getBudgetLimitProgressUseCase(),
        observeCustomCategoriesUseCase(),
        observeActiveWalletUseCase(),
    ) { progress, customCategories, activeWallet ->
        val builtinOptions = Category.entries
            .filter { it.type == TransactionType.EXPENSE }
            .map { BudgetCategoryOption.Builtin(it) }
        val customOptions = customCategories
            .filter { it.type == TransactionType.EXPENSE }
            .map { BudgetCategoryOption.Custom(it) }
        BudgetLimitsUiState(
            progress = progress,
            availableOptions = builtinOptions + customOptions,
            customCategories = customCategories,
            activeWalletId = activeWallet?.id,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetLimitsUiState())

    private val _dialogState = MutableStateFlow<BudgetLimitDialogState>(BudgetLimitDialogState.Closed)
    val dialogState: StateFlow<BudgetLimitDialogState> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = BudgetLimitDialogState.Adding()
    }

    fun updateDraft(transform: (BudgetLimitDialogState.Adding) -> BudgetLimitDialogState.Adding) {
        val current = _dialogState.value
        if (current is BudgetLimitDialogState.Adding) _dialogState.value = transform(current)
    }

    fun dismissDialog() {
        _dialogState.value = BudgetLimitDialogState.Closed
    }

    fun confirmAdd() {
        val state = _dialogState.value
        if (state !is BudgetLimitDialogState.Adding) return
        val option = state.option ?: return
        val amount = state.amountText.toDoubleOrNull() ?: return
        if (amount <= 0) return
        val walletId = uiState.value.activeWalletId ?: return

        val existing = uiState.value.progress.map { it.limit }.firstOrNull { limit ->
            when (option) {
                is BudgetCategoryOption.Builtin -> limit.customCategoryId == null && limit.category == option.category
                is BudgetCategoryOption.Custom -> limit.customCategoryId == option.customCategory.id
            }
        }
        // Changing the limit amount resets the notification watermark, so raising a limit after
        // an "approaching"/"exceeded" alert can trigger a fresh alert once the new amount is hit.
        val amountChanged = existing != null && existing.monthlyLimit != amount
        val limit = CategoryBudgetLimit(
            id = existing?.id ?: 0,
            category = (option as? BudgetCategoryOption.Builtin)?.category,
            customCategoryId = (option as? BudgetCategoryOption.Custom)?.customCategory?.id,
            monthlyLimit = amount,
            walletId = walletId,
            lastNotifiedMonth = if (amountChanged) null else existing?.lastNotifiedMonth,
            lastNotifiedThreshold = if (amountChanged) null else existing?.lastNotifiedThreshold,
        )
        viewModelScope.launch {
            setBudgetLimitUseCase(limit)
            _dialogState.value = BudgetLimitDialogState.Closed
        }
    }

    fun delete(limit: CategoryBudgetLimit) {
        viewModelScope.launch { deleteBudgetLimitUseCase(limit) }
    }
}
