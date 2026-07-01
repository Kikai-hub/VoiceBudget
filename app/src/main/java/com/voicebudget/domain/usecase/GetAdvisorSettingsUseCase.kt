package com.voicebudget.domain.usecase

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.repository.AdvisorSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAdvisorSettingsUseCase @Inject constructor(
    private val repository: AdvisorSettingsRepository,
) {
    operator fun invoke(): Flow<AdvisorSettings> = repository.observeSettings()
}
