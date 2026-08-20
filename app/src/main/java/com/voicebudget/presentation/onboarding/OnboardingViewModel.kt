package com.voicebudget.presentation.onboarding

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.R
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.usecase.CreateWalletUseCase
import com.voicebudget.domain.usecase.UpdateRecognitionLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val updateRecognitionLanguageUseCase: UpdateRecognitionLanguageUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(walletName = context.getString(R.string.wallet_default_name, 1)),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Persist the language pre-selected in the UI so it takes effect even if the user
        // taps "Next" on the language step without explicitly tapping a language row.
        selectLanguage(_uiState.value.selectedLanguageTag)
    }

    fun selectLanguage(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        viewModelScope.launch { updateRecognitionLanguageUseCase(tag) }
        _uiState.value = _uiState.value.copy(selectedLanguageTag = tag)
    }

    fun goToCurrencyStep() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.CURRENCY)
    }

    fun selectCurrency(currency: Currency) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currency)
    }

    fun goToWalletStep() {
        _uiState.value = _uiState.value.copy(step = OnboardingStep.WALLET)
    }

    fun setWalletName(name: String) {
        _uiState.value = _uiState.value.copy(walletName = name)
    }

    fun createWallet() {
        val state = _uiState.value
        if (state.walletName.isBlank() || state.isCreating) return
        _uiState.value = state.copy(isCreating = true)
        viewModelScope.launch {
            createWalletUseCase(state.walletName.trim(), state.selectedCurrency)
            _uiState.value = _uiState.value.copy(isCreating = false)
        }
    }
}
