package com.voicebudget.domain.usecase

import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.repository.AdvisorSettingsRepository
import javax.inject.Inject

class UpdateAdvisorSettingsUseCase @Inject constructor(
    private val repository: AdvisorSettingsRepository,
) {
    suspend operator fun invoke(settings: AdvisorSettings) = repository.updateSettings(settings)
}
