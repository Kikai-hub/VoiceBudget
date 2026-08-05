package com.voicebudget.data.repository

import com.voicebudget.data.database.FinancialGoalDao
import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: FinancialGoalDao,
) : GoalRepository {

    override fun observeAll(): Flow<List<FinancialGoal>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(goalId: Long): FinancialGoal? = dao.getById(goalId)?.toDomain()

    override suspend fun add(goal: FinancialGoal): Long = dao.insert(goal.toEntity())

    override suspend fun delete(goal: FinancialGoal) = dao.delete(goal.toEntity())

    override suspend fun contribute(goalId: Long, amount: Double) = dao.contribute(goalId, amount)
}
