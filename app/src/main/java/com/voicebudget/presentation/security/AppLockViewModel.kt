package com.voicebudget.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.ObserveSecuritySettingsUseCase
import com.voicebudget.domain.usecase.VerifyPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val PIN_LENGTH = 4

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockState: AppLockState,
    private val verifyPinUseCase: VerifyPinUseCase,
    observeSecuritySettingsUseCase: ObserveSecuritySettingsUseCase,
) : ViewModel() {

    val isBiometricEnabled: StateFlow<Boolean> = observeSecuritySettingsUseCase()
        .map { it.isBiometricEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    fun onPinChange(value: String) {
        if (value.length > PIN_LENGTH || value.any { !it.isDigit() }) return
        _error.value = false
        _pin.value = value
        if (value.length == PIN_LENGTH) tryUnlock()
    }

    fun onBiometricSuccess() {
        appLockState.unlock()
    }

    private fun tryUnlock() {
        viewModelScope.launch {
            if (verifyPinUseCase(_pin.value)) {
                appLockState.unlock()
            } else {
                _error.value = true
                _pin.value = ""
            }
        }
    }
}
