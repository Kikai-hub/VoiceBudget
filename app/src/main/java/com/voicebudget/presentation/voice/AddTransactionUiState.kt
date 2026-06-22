package com.voicebudget.presentation.voice

sealed interface AddTransactionUiState {
    data object Idle : AddTransactionUiState
    data object Listening : AddTransactionUiState
    data class Confirming(val draft: TransactionDraft) : AddTransactionUiState
    data class Error(val message: String) : AddTransactionUiState
    data object Saving : AddTransactionUiState
    data object Saved : AddTransactionUiState
}
