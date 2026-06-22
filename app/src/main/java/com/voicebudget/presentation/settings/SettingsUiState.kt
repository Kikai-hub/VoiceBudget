package com.voicebudget.presentation.settings

import com.voicebudget.domain.model.AppSettings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(),
)
