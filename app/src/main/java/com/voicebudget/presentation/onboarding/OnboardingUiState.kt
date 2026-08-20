package com.voicebudget.presentation.onboarding

import com.voicebudget.domain.model.Currency

enum class OnboardingStep { LANGUAGE, CURRENCY, WALLET }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.LANGUAGE,
    val selectedLanguageTag: String = "en-US",
    val selectedCurrency: Currency = Currency.USD,
    val walletName: String = "",
    val isCreating: Boolean = false,
)
