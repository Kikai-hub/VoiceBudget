package com.voicebudget.fakes

import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {

    private val state = MutableStateFlow(initial)

    override fun observeSettings() = state

    override suspend fun setCurrency(currency: Currency) {
        state.value = state.value.copy(currency = currency)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }

    override suspend fun setRecognitionLanguageTag(languageTag: String) {
        state.value = state.value.copy(recognitionLanguageTag = languageTag)
    }
}
