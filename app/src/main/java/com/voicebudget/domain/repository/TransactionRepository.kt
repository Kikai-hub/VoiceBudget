package com.voicebudget.domain.repository

import com.voicebudget.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    suspend fun add(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun clearAll()
}
