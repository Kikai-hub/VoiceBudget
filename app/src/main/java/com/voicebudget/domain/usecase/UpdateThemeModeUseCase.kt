package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateThemeModeUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(themeMode: ThemeMode) = repository.setThemeMode(themeMode)
}
