package com.voicebudget.data.repository

import com.voicebudget.data.database.FinancialGoalDao
import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GoalRepositoryImpl @Inject constructor(
    private val dao: FinancialGoalDao,
    private val walletRepository: WalletRepository,
) : GoalRepository {

    override fun observeAll(): Flow<List<FinancialGoal>> =
        walletRepository.observeActiveWalletId().flatMapLatest { walletId ->
            if (walletId == null) return@flatMapLatest flowOf(emptyList())
            dao.getAllForWallet(walletId).map { entities -> entities.map { it.toDomain() } }
        }

    override suspend fun getById(goalId: Long): FinancialGoal? = dao.getById(goalId)?.toDomain()

    override suspend fun add(goal: FinancialGoal): Long {
        val walletId = walletRepository.getActiveWalletId() ?: 0
        return dao.insert(goal.toEntity().copy(walletId = walletId))
    }

    override suspend fun delete(goal: FinancialGoal) = dao.delete(goal.toEntity())

    override suspend fun contribute(goalId: Long, amount: Double) = dao.contribute(goalId, amount)
}
