package com.voicebudget.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

private object SettingsKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val RECOGNITION_LANGUAGE_TAG = stringPreferencesKey("recognition_language_tag")
}

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val walletRepository: WalletRepository,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> =
        combine(dataStore.data, walletRepository.observeActiveWallet()) { preferences, activeWallet ->
            AppSettings(
                currency = activeWallet?.currency ?: Currency.RUB,
                themeMode = preferences[SettingsKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                recognitionLanguageTag = preferences[SettingsKeys.RECOGNITION_LANGUAGE_TAG] ?: "ru-RU",
            )
        }

    override suspend fun setCurrency(currency: Currency) {
        walletRepository.updateActiveWalletCurrency(currency)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[SettingsKeys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setRecognitionLanguageTag(languageTag: String) {
        dataStore.edit { it[SettingsKeys.RECOGNITION_LANGUAGE_TAG] = languageTag }
    }
}
