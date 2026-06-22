package com.voicebudget.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = observeSettingsUseCase()
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)
}
