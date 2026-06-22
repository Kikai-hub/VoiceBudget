package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}
