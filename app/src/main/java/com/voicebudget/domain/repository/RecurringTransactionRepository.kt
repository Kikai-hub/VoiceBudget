package com.voicebudget.domain.repository

import com.voicebudget.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    /** Rules belonging to the currently active wallet, for the management UI. */
    fun observeForActiveWallet(): Flow<List<RecurringTransaction>>

    suspend fun create(recurring: RecurringTransaction): Long
    suspend fun update(recurring: RecurringTransaction)
    suspend fun delete(recurring: RecurringTransaction)

    /** Active rules due to run, across every wallet — used by the materialization worker. */
    suspend fun getDue(nowMillis: Long): List<RecurringTransaction>
}
