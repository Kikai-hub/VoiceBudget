package com.voicebudget.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voicebudget.domain.repository.SecurityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

private val PIN_HASH_KEY = stringPreferencesKey("security_pin_hash")
private val PIN_SALT_KEY = stringPreferencesKey("security_pin_salt")
private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("security_biometric_enabled")

class SecurityRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SecurityRepository {

    override fun isPinSet(): Flow<Boolean> = dataStore.data.map { it[PIN_HASH_KEY] != null }

    override fun isBiometricEnabled(): Flow<Boolean> = dataStore.data.map { it[BIOMETRIC_ENABLED_KEY] ?: false }

    override suspend fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = hash(pin, salt)
        dataStore.edit {
            it[PIN_SALT_KEY] = salt
            it[PIN_HASH_KEY] = hash
        }
    }

    override suspend fun clearPin() {
        dataStore.edit {
            it.remove(PIN_HASH_KEY)
            it.remove(PIN_SALT_KEY)
            it.remove(BIOMETRIC_ENABLED_KEY)
        }
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val prefs = dataStore.data.first()
        val salt = prefs[PIN_SALT_KEY] ?: return false
        val expectedHash = prefs[PIN_HASH_KEY] ?: return false
        return hash(pin, salt) == expectedHash
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED_KEY] = enabled }
    }

    private suspend fun hash(pin: String, saltHex: String): String = withContext(Dispatchers.Default) {
        val salt = hexToBytes(saltHex)
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        bytesToHex(factory.generateSecret(spec).encoded)
    }

    private fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return bytesToHex(salt)
    }

    private fun bytesToHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { i -> ((Character.digit(hex[i * 2], 16) shl 4) + Character.digit(hex[i * 2 + 1], 16)).toByte() }

    private companion object {
        const val PBKDF2_ITERATIONS = 120_000
        const val PBKDF2_KEY_LENGTH_BITS = 256
    }
}
