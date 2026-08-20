package com.voicebudget.data.repository

import com.voicebudget.data.database.RecurringTransactionDao
import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val dao: RecurringTransactionDao,
    private val walletRepository: WalletRepository,
) : RecurringTransactionRepository {

    override fun observeForActiveWallet(): Flow<List<RecurringTransaction>> =
        walletRepository.observeActiveWalletId().flatMapLatest { walletId ->
            if (walletId == null) return@flatMapLatest flowOf(emptyList())
            dao.getAllForWallet(walletId).map { entities -> entities.map { it.toDomain() } }
        }

    override suspend fun create(recurring: RecurringTransaction): Long = dao.insert(recurring.toEntity())

    override suspend fun update(recurring: RecurringTransaction) = dao.update(recurring.toEntity())

    override suspend fun delete(recurring: RecurringTransaction) = dao.delete(recurring.toEntity())

    override suspend fun getDue(nowMillis: Long): List<RecurringTransaction> =
        dao.getDue(nowMillis).map { it.toDomain() }
}
