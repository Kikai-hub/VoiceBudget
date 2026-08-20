package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionDao
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val walletRepository: WalletRepository,
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> =
        walletRepository.observeActiveWalletId().flatMapLatest { walletId ->
            if (walletId == null) return@flatMapLatest flowOf(emptyList())
            dao.getAllForWallet(walletId).map { entities -> entities.map { it.toDomain() } }
        }

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun add(transaction: Transaction): Long {
        val walletId = walletRepository.getActiveWalletId() ?: 0
        return dao.insert(transaction.toEntity().copy(walletId = walletId))
    }

    override suspend fun update(transaction: Transaction) {
        val walletId = dao.getById(transaction.id)?.walletId ?: walletRepository.getActiveWalletId() ?: 0
        dao.update(transaction.toEntity().copy(walletId = walletId))
    }

    override suspend fun delete(transaction: Transaction) = dao.delete(transaction.toEntity())

    override suspend fun clearAll() = dao.deleteAll()

    override suspend fun clearCustomCategory(categoryId: Long) = dao.clearCustomCategory(categoryId)

    override suspend fun clearGoal(goalId: Long) = dao.clearGoal(goalId)

    override suspend fun getLastTransactionTime(): Long? = dao.getLastTransactionTime()

    override suspend fun addToWallet(transaction: Transaction, walletId: Long): Long =
        dao.insert(transaction.toEntity().copy(walletId = walletId))

    override suspend fun getAllForWallet(walletId: Long): List<Transaction> =
        dao.getAllForWalletOnce(walletId).map { it.toDomain() }

    override fun observeAllForWallet(walletId: Long): Flow<List<Transaction>> =
        dao.getAllForWallet(walletId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTransferPair(
        out: Transaction,
        outWalletId: Long,
        into: Transaction,
        intoWalletId: Long,
    ) = dao.insertTransferPair(
        out.toEntity().copy(walletId = outWalletId),
        into.toEntity().copy(walletId = intoWalletId),
    )
}
