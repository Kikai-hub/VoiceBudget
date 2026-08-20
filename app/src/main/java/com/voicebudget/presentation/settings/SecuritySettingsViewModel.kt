package com.voicebudget.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.ClearPinUseCase
import com.voicebudget.domain.usecase.ObserveSecuritySettingsUseCase
import com.voicebudget.domain.usecase.SecuritySettings
import com.voicebudget.domain.usecase.SetBiometricEnabledUseCase
import com.voicebudget.domain.usecase.SetPinUseCase
import com.voicebudget.presentation.security.PIN_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PinSetupState {
    data object Closed : PinSetupState
    data class EnteringNew(val pin: String = "") : PinSetupState
    data class Confirming(val newPin: String, val confirmPin: String = "", val mismatch: Boolean = false) : PinSetupState
}

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    observeSecuritySettingsUseCase: ObserveSecuritySettingsUseCase,
    private val setPinUseCase: SetPinUseCase,
    private val clearPinUseCase: ClearPinUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
) : ViewModel() {

    val settings: StateFlow<SecuritySettings> = observeSecuritySettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecuritySettings(false, false))

    private val _pinSetupState = MutableStateFlow<PinSetupState>(PinSetupState.Closed)
    val pinSetupState: StateFlow<PinSetupState> = _pinSetupState.asStateFlow()

    fun startSettingPin() {
        _pinSetupState.value = PinSetupState.EnteringNew()
    }

    fun onNewPinChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(PIN_LENGTH)
        _pinSetupState.value = PinSetupState.EnteringNew(digits)
        if (digits.length == PIN_LENGTH) {
            _pinSetupState.value = PinSetupState.Confirming(newPin = digits)
        }
    }

    fun onConfirmPinChange(value: String) {
        val state = _pinSetupState.value
        if (state !is PinSetupState.Confirming) return
        val digits = value.filter { it.isDigit() }.take(PIN_LENGTH)
        _pinSetupState.value = state.copy(confirmPin = digits, mismatch = false)
        if (digits.length == PIN_LENGTH) {
            if (digits == state.newPin) {
                viewModelScope.launch {
                    setPinUseCase(digits)
                    _pinSetupState.value = PinSetupState.Closed
                }
            } else {
                _pinSetupState.value = state.copy(confirmPin = "", mismatch = true)
            }
        }
    }

    fun cancelPinSetup() {
        _pinSetupState.value = PinSetupState.Closed
    }

    fun disableProtection() {
        viewModelScope.launch { clearPinUseCase() }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { setBiometricEnabledUseCase(enabled) }
    }
}
