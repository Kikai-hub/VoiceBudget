package com.voicebudget.presentation.advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.usecase.DismissAdviceUseCase
import com.voicebudget.domain.usecase.GetAdvisorSettingsUseCase
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.UpdateAdvisorSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvisorViewModel @Inject constructor(
    getFinancialAdviceUseCase: GetFinancialAdviceUseCase,
    getAdvisorSettingsUseCase: GetAdvisorSettingsUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val dismissAdviceUseCase: DismissAdviceUseCase,
    private val updateAdvisorSettingsUseCase: UpdateAdvisorSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<AdvisorUiState> = combine(
        getFinancialAdviceUseCase(),
        getAdvisorSettingsUseCase(),
        observeSettingsUseCase(),
    ) { advice, advisorSettings, appSettings ->
        AdvisorUiState(
            isLoading = false,
            advice = advice.filterNot { it.dismissed },
            settings = advisorSettings,
            currencySymbol = appSettings.currency.symbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdvisorUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun dismiss(id: String) {
        viewModelScope.launch { dismissAdviceUseCase(id) }
    }

    fun updateSettings(settings: AdvisorSettings) {
        viewModelScope.launch {
            updateAdvisorSettingsUseCase(settings)
            _message.value = "Настройки сохранены"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
