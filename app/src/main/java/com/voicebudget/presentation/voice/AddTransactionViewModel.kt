package com.voicebudget.presentation.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.R
import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.parser.ParseFailureReason
import com.voicebudget.domain.parser.ParseResult
import com.voicebudget.domain.usecase.AddTransactionUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.ParseVoiceInputUseCase
import com.voicebudget.presentation.components.categoryLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val voiceRecognizerService: VoiceRecognizerService,
    private val parseVoiceInputUseCase: ParseVoiceInputUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val settings: StateFlow<AppSettings> = observeSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var recognitionJob: Job? = null

    fun startListening(languageTag: String = settings.value.recognitionLanguageTag) {
        recognitionJob?.cancel()
        _uiState.value = AddTransactionUiState.Listening
        recognitionJob = viewModelScope.launch {
            voiceRecognizerService.listen(languageTag).collect { event ->
                when (event) {
                    is RecognitionEvent.FinalResult -> handleRecognizedText(event.text)
                    is RecognitionEvent.Error -> _uiState.value = AddTransactionUiState.Error(event.message)
                    is RecognitionEvent.PartialResult, RecognitionEvent.ReadyForSpeech -> Unit
                }
            }
        }
    }

    fun startManualEntry() {
        recognitionJob?.cancel()
        _uiState.value = AddTransactionUiState.Confirming(
            TransactionDraft(
                amountText = "",
                type = TransactionType.EXPENSE,
                category = Category.OTHER_EXPENSE,
                description = "",
            ),
        )
    }

    fun updateDraft(transform: (TransactionDraft) -> TransactionDraft) {
        val current = _uiState.value
        if (current is AddTransactionUiState.Confirming) {
            _uiState.value = current.copy(draft = transform(current.draft))
        }
    }

    fun confirm() {
        val current = _uiState.value
        if (current !is AddTransactionUiState.Confirming) return
        val amount = current.draft.amountText.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.value = AddTransactionUiState.Error(context.getString(R.string.error_invalid_amount))
            return
        }

        _uiState.value = AddTransactionUiState.Saving
        viewModelScope.launch {
            addTransactionUseCase(
                Transaction(
                    amount = amount,
                    type = current.draft.type,
                    category = current.draft.category,
                    description = current.draft.description.ifBlank { categoryLabel(context, current.draft.category) },
                    createdAt = System.currentTimeMillis(),
                ),
            )
            _uiState.value = AddTransactionUiState.Saved
        }
    }

    fun retry() {
        recognitionJob?.cancel()
        _uiState.value = AddTransactionUiState.Idle
    }

    private fun handleRecognizedText(text: String) {
        when (val result = parseVoiceInputUseCase(text)) {
            is ParseResult.Success -> _uiState.value = AddTransactionUiState.Confirming(result.transaction.toDraft())
            is ParseResult.Failure -> _uiState.value = AddTransactionUiState.Error(failureMessage(result.reason, text))
        }
    }

    private fun failureMessage(reason: ParseFailureReason, text: String): String = when (reason) {
        ParseFailureReason.EMPTY_INPUT -> context.getString(R.string.error_no_speech_match)
        ParseFailureReason.AMOUNT_NOT_FOUND -> context.getString(R.string.error_amount_not_found, text)
    }

    override fun onCleared() {
        recognitionJob?.cancel()
    }
}
