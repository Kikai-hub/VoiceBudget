package com.voicebudget.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.voicebudget.R
import androidx.lifecycle.viewModelScope
import com.voicebudget.data.csv.TransactionCsv
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.usecase.AddTransactionUseCase
import com.voicebudget.domain.usecase.ClearAllDataUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.UpdateCurrencyUseCase
import com.voicebudget.domain.usecase.UpdateRecognitionLanguageUseCase
import com.voicebudget.domain.usecase.UpdateThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateCurrencyUseCase: UpdateCurrencyUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val updateRecognitionLanguageUseCase: UpdateRecognitionLanguageUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observeSettingsUseCase()
        .map { SettingsUiState(isLoading = false, settings = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { updateCurrencyUseCase(currency) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { updateThemeModeUseCase(themeMode) }
    }

    fun setRecognitionLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        viewModelScope.launch { updateRecognitionLanguageUseCase(languageTag) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            clearAllDataUseCase()
            _message.value = context.getString(R.string.msg_all_data_cleared)
        }
    }

    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val transactions = getTransactionsUseCase().first()
                val csv = TransactionCsv.toCsv(transactions)
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    ?: error("Could not open file for writing")
            }.onSuccess {
                _message.value = context.getString(R.string.msg_export_success)
            }.onFailure {
                _message.value = context.getString(R.string.msg_export_failed, it.message.toString())
            }
        }
    }

    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                    ?: error("Could not open file for reading")
                val transactions = TransactionCsv.fromCsv(content)
                transactions.forEach { addTransactionUseCase(it) }
                transactions.size
            }.onSuccess { count ->
                _message.value = context.getString(R.string.msg_import_success, count)
            }.onFailure {
                _message.value = context.getString(R.string.msg_import_failed, it.message.toString())
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
