package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionDao
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun add(transaction: Transaction): Long = dao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) = dao.update(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) = dao.delete(transaction.toEntity())

    override suspend fun clearAll() = dao.deleteAll()

    override suspend fun clearCustomCategory(categoryId: Long) = dao.clearCustomCategory(categoryId)

    override suspend fun clearGoal(goalId: Long) = dao.clearGoal(goalId)

    override suspend fun getLastTransactionTime(): Long? = dao.getLastTransactionTime()
}
