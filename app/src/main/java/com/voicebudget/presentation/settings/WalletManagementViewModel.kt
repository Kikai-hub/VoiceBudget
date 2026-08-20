package com.voicebudget.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.R
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.MAX_WALLETS
import com.voicebudget.domain.model.Wallet
import com.voicebudget.domain.usecase.ConvertCurrencyUseCase
import com.voicebudget.domain.usecase.CreateWalletUseCase
import com.voicebudget.domain.usecase.ObserveActiveWalletUseCase
import com.voicebudget.domain.usecase.ObserveWalletsUseCase
import com.voicebudget.domain.usecase.SetActiveWalletUseCase
import com.voicebudget.domain.usecase.TransferBetweenWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletManagementUiState(
    val wallets: List<Wallet> = emptyList(),
    val activeWalletId: Long? = null,
) {
    val canAddWallet: Boolean get() = wallets.size < MAX_WALLETS
}

data class TransferDraft(
    val fromWallet: Wallet,
    val toWallet: Wallet,
    val fromAmountText: String = "",
    val toAmountText: String = "",
    val toAmountEditedManually: Boolean = false,
    val isSubmitting: Boolean = false,
    val rateUnavailable: Boolean = false,
)

sealed interface WalletDialogState {
    data object Closed : WalletDialogState
    data class Adding(val name: String = "", val currency: Currency = Currency.USD) : WalletDialogState
    data class Transferring(val draft: TransferDraft) : WalletDialogState
}

@HiltViewModel
class WalletManagementViewModel @Inject constructor(
    observeWalletsUseCase: ObserveWalletsUseCase,
    observeActiveWalletUseCase: ObserveActiveWalletUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val setActiveWalletUseCase: SetActiveWalletUseCase,
    private val transferBetweenWalletsUseCase: TransferBetweenWalletsUseCase,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    val uiState: StateFlow<WalletManagementUiState> = combine(
        observeWalletsUseCase(),
        observeActiveWalletUseCase(),
    ) { wallets, activeWallet ->
        WalletManagementUiState(wallets = wallets, activeWalletId = activeWallet?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalletManagementUiState())

    private val _dialogState = MutableStateFlow<WalletDialogState>(WalletDialogState.Closed)
    val dialogState: StateFlow<WalletDialogState> = _dialogState.asStateFlow()

    fun switchTo(walletId: Long) {
        viewModelScope.launch { setActiveWalletUseCase(walletId) }
    }

    fun openAddDialog() {
        _dialogState.value = WalletDialogState.Adding()
    }

    fun updateDraft(transform: (WalletDialogState.Adding) -> WalletDialogState.Adding) {
        val current = _dialogState.value
        if (current is WalletDialogState.Adding) {
            _dialogState.value = transform(current)
        }
    }

    fun dismissDialog() {
        _dialogState.value = WalletDialogState.Closed
    }

    fun confirmAdd() {
        val state = _dialogState.value
        if (state !is WalletDialogState.Adding || state.name.isBlank()) return
        viewModelScope.launch {
            val result = createWalletUseCase(state.name.trim(), state.currency)
            if (result.isSuccess) {
                _dialogState.value = WalletDialogState.Closed
            }
        }
    }

    fun openTransferDialog() {
        val wallets = uiState.value.wallets
        val active = wallets.firstOrNull { it.id == uiState.value.activeWalletId } ?: wallets.firstOrNull()
        val other = wallets.firstOrNull { it.id != active?.id }
        if (active == null || other == null) return
        _dialogState.value = WalletDialogState.Transferring(TransferDraft(fromWallet = active, toWallet = other))
    }

    fun swapTransferDirection() {
        val state = _dialogState.value
        if (state !is WalletDialogState.Transferring) return
        val draft = state.draft
        _dialogState.value = WalletDialogState.Transferring(
            draft.copy(
                fromWallet = draft.toWallet,
                toWallet = draft.fromWallet,
                fromAmountText = draft.toAmountText,
                toAmountText = draft.fromAmountText,
                toAmountEditedManually = false,
            ),
        )
    }

    fun updateTransferFromAmount(text: String) {
        val state = _dialogState.value
        if (state !is WalletDialogState.Transferring) return
        val draft = state.draft.copy(fromAmountText = text)
        _dialogState.value = WalletDialogState.Transferring(draft)
        if (!draft.toAmountEditedManually) recomputeToAmount(draft)
    }

    fun updateTransferToAmount(text: String) {
        val state = _dialogState.value
        if (state !is WalletDialogState.Transferring) return
        _dialogState.value = WalletDialogState.Transferring(
            state.draft.copy(toAmountText = text, toAmountEditedManually = true, rateUnavailable = false),
        )
    }

    private fun recomputeToAmount(draft: TransferDraft) {
        val amount = draft.fromAmountText.toDoubleOrNull()
        if (draft.fromWallet.currency == draft.toWallet.currency) {
            _dialogState.value = WalletDialogState.Transferring(
                draft.copy(toAmountText = draft.fromAmountText, rateUnavailable = false),
            )
            return
        }
        if (amount == null) return
        viewModelScope.launch {
            val converted = convertCurrencyUseCase(amount, draft.fromWallet.currency, draft.toWallet.currency)
            val current = _dialogState.value
            if (current !is WalletDialogState.Transferring || current.draft.toAmountEditedManually) return@launch
            _dialogState.value = WalletDialogState.Transferring(
                current.draft.copy(
                    toAmountText = converted?.let { "%.2f".format(it) } ?: current.draft.toAmountText,
                    rateUnavailable = converted == null,
                ),
            )
        }
    }

    fun confirmTransfer() {
        val state = _dialogState.value
        if (state !is WalletDialogState.Transferring) return
        val draft = state.draft
        val fromAmount = draft.fromAmountText.toDoubleOrNull()
        val toAmount = draft.toAmountText.toDoubleOrNull()
        if (fromAmount == null || fromAmount <= 0 || toAmount == null || toAmount <= 0) return
        _dialogState.value = WalletDialogState.Transferring(draft.copy(isSubmitting = true))
        viewModelScope.launch {
            transferBetweenWalletsUseCase(
                fromWalletId = draft.fromWallet.id,
                fromAmount = fromAmount,
                outDescription = context.getString(R.string.transaction_transfer_to, draft.toWallet.name),
                toWalletId = draft.toWallet.id,
                toAmount = toAmount,
                inDescription = context.getString(R.string.transaction_transfer_from, draft.fromWallet.name),
            )
            _dialogState.value = WalletDialogState.Closed
        }
    }
}
