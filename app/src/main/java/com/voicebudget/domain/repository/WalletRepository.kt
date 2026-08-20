package com.voicebudget.domain.repository

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeWallets(): Flow<List<Wallet>>
    fun observeActiveWallet(): Flow<Wallet?>
    fun observeActiveWalletId(): Flow<Long?>

    /** Resolves the active wallet id, falling back to the first wallet if none is explicitly set yet. */
    suspend fun getActiveWalletId(): Long?
    suspend fun getWalletById(id: Long): Wallet?
    suspend fun createWallet(name: String, currency: Currency): Result<Wallet>
    suspend fun setActiveWallet(id: Long)
    suspend fun updateActiveWalletCurrency(currency: Currency)

    /**
     * Runs once at app startup. If the install has legacy data (transactions/goals) predating
     * wallets but no wallet row yet, silently creates a "Wallet 1" so existing users skip
     * onboarding. No-ops for a genuinely fresh install (onboarding creates the first wallet).
     */
    suspend fun ensureDefaultWallet()
}

class WalletLimitReachedException : Exception("Maximum number of wallets reached")
