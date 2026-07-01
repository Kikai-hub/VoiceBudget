package com.voicebudget.fakes

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.repository.AdvisorSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAdvisorSettingsRepository(
    initialSettings: AdvisorSettings = AdvisorSettings(),
) : AdvisorSettingsRepository {

    private val settingsFlow = MutableStateFlow(initialSettings)
    private val dismissedFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun observeSettings(): Flow<AdvisorSettings> = settingsFlow
    override fun observeDismissedIds(): Flow<Set<String>> = dismissedFlow

    override suspend fun updateSettings(settings: AdvisorSettings) {
        settingsFlow.value = settings
    }

    override suspend fun dismissAdvice(id: String) {
        dismissedFlow.value = dismissedFlow.value + id
    }

    override suspend fun restoreAdvice(id: String) {
        dismissedFlow.value = dismissedFlow.value - id
    }
}
