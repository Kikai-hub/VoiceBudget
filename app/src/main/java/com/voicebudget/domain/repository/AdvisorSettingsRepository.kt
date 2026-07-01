package com.voicebudget.domain.repository

import com.voicebudget.domain.advisor.AdvisorSettings
import kotlinx.coroutines.flow.Flow

interface AdvisorSettingsRepository {
    fun observeSettings(): Flow<AdvisorSettings>
    fun observeDismissedIds(): Flow<Set<String>>
    suspend fun updateSettings(settings: AdvisorSettings)
    suspend fun dismissAdvice(id: String)
    suspend fun restoreAdvice(id: String)
}
