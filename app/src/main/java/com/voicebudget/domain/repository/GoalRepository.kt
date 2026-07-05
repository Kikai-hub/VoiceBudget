package com.voicebudget.domain.repository

import com.voicebudget.domain.goals.FinancialGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeAll(): Flow<List<FinancialGoal>>
    suspend fun add(goal: FinancialGoal): Long
    suspend fun delete(goal: FinancialGoal)
}
