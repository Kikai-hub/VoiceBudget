package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SecuritySettings(val isPinSet: Boolean, val isBiometricEnabled: Boolean)

class ObserveSecuritySettingsUseCase @Inject constructor(
    private val repository: SecurityRepository,
) {
    operator fun invoke(): Flow<SecuritySettings> =
        combine(repository.isPinSet(), repository.isBiometricEnabled()) { pinSet, biometricEnabled ->
            SecuritySettings(pinSet, biometricEnabled)
        }
}

class SetPinUseCase @Inject constructor(private val repository: SecurityRepository) {
    suspend operator fun invoke(pin: String) = repository.setPin(pin)
}

class ClearPinUseCase @Inject constructor(private val repository: SecurityRepository) {
    suspend operator fun invoke() = repository.clearPin()
}

class VerifyPinUseCase @Inject constructor(private val repository: SecurityRepository) {
    suspend operator fun invoke(pin: String): Boolean = repository.verifyPin(pin)
}

class SetBiometricEnabledUseCase @Inject constructor(private val repository: SecurityRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setBiometricEnabled(enabled)
}
