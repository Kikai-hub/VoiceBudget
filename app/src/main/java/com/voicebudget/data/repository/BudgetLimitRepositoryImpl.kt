package com.voicebudget.data.repository

import com.voicebudget.data.database.CategoryBudgetLimitDao
import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetLimitRepositoryImpl @Inject constructor(
    private val dao: CategoryBudgetLimitDao,
    private val walletRepository: WalletRepository,
) : BudgetLimitRepository {

    override fun observeForActiveWallet(): Flow<List<CategoryBudgetLimit>> =
        walletRepository.observeActiveWalletId().flatMapLatest { walletId ->
            if (walletId == null) return@flatMapLatest flowOf(emptyList())
            dao.getAllForWallet(walletId).map { entities -> entities.map { it.toDomain() } }
        }

    override fun observeAll(): Flow<List<CategoryBudgetLimit>> =
        walletRepository.observeWallets().flatMapLatest { wallets ->
            if (wallets.isEmpty()) return@flatMapLatest flowOf(emptyList())
            val perWallet = wallets.map { wallet -> dao.getAllForWallet(wallet.id) }
            combine(perWallet) { arrays -> arrays.flatMap { it }.map { it.toDomain() } }
        }

    override suspend fun setLimit(limit: CategoryBudgetLimit): Long = dao.insert(limit.toEntity())

    override suspend fun delete(limit: CategoryBudgetLimit) = dao.delete(limit.toEntity())

    override suspend fun markNotified(id: Long, month: String, threshold: Int) = dao.markNotified(id, month, threshold)
}
