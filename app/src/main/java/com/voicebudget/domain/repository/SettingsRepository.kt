package com.voicebudget.domain.repository

import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setCurrency(currency: Currency)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setRecognitionLanguageTag(languageTag: String)
}
