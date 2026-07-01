package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.AdvisorSettingsRepository
import javax.inject.Inject

class DismissAdviceUseCase @Inject constructor(
    private val repository: AdvisorSettingsRepository,
) {
    suspend operator fun invoke(adviceId: String) = repository.dismissAdvice(adviceId)
}
