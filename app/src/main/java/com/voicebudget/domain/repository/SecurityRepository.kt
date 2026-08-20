package com.voicebudget.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    fun isPinSet(): Flow<Boolean>
    fun isBiometricEnabled(): Flow<Boolean>

    suspend fun setPin(pin: String)
    suspend fun clearPin()
    suspend fun verifyPin(pin: String): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)
}
