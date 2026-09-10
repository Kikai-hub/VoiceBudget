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
import com.voicebudget.presentation.components.languageOptions
import com.voicebudget.utils.withAppLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val updateRecognitionLanguageUseCase: UpdateRecognitionLanguageUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            selectedLanguageTag = deviceDefaultLanguageTag(context),
            walletName = context.withAppLocale().getString(R.string.wallet_default_name, 1),
        ),
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
        // Re-derive the default wallet name so it reflects the newly applied locale — it's only
        // ever a stand-in the user hasn't typed over yet at this point in the flow (the language
        // step always runs before the wallet-naming step).
        _uiState.value = _uiState.value.copy(
            selectedLanguageTag = tag,
            walletName = context.withAppLocale().getString(R.string.wallet_default_name, 1),
        )
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

/**
 * Pre-selects the onboarding language step with the device's current system language when it's
 * one of the app's supported languages, instead of always defaulting to English — matched by
 * language subtag alone since [languageOptions] pins a specific region per language (e.g.
 * "es-ES") that won't equal a device locale like "es-MX".
 */
private fun deviceDefaultLanguageTag(context: Context): String {
    val deviceLanguage = context.resources.configuration.locales.get(0).language
    return languageOptions.firstOrNull { (tag, _) -> Locale.forLanguageTag(tag).language == deviceLanguage }
        ?.first
        ?: "en-US"
}
