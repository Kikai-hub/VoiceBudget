package com.voicebudget.domain.repository

import com.voicebudget.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun add(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun clearAll()
    suspend fun clearCustomCategory(categoryId: Long)
    suspend fun clearGoal(goalId: Long)
    suspend fun getLastTransactionTime(): Long?

    /** Inserts into a specific wallet, bypassing the active-wallet convention [add] uses. */
    suspend fun addToWallet(transaction: Transaction, walletId: Long): Long

    /** All-time transactions of one wallet, regardless of which wallet is currently active. */
    suspend fun getAllForWallet(walletId: Long): List<Transaction>

    /** Reactive version of [getAllForWallet], for a specific wallet regardless of which is active. */
    fun observeAllForWallet(walletId: Long): Flow<List<Transaction>>

    /** Atomically records both legs of a transfer between two wallets. */
    suspend fun addTransferPair(out: Transaction, outWalletId: Long, into: Transaction, intoWalletId: Long)
}
