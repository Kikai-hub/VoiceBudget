package com.voicebudget.fakes

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGoalRepository(
    initial: List<FinancialGoal> = emptyList(),
) : GoalRepository {

    private val goalsFlow = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeAll(): Flow<List<FinancialGoal>> = goalsFlow

    override suspend fun getById(goalId: Long): FinancialGoal? = goalsFlow.value.find { it.id == goalId }

    override suspend fun add(goal: FinancialGoal): Long {
        val id = nextId++
        goalsFlow.value = goalsFlow.value + goal.copy(id = id)
        return id
    }

    override suspend fun delete(goal: FinancialGoal) {
        goalsFlow.value = goalsFlow.value.filterNot { it.id == goal.id }
    }

    override suspend fun contribute(goalId: Long, amount: Double) {
        goalsFlow.value = goalsFlow.value.map { goal ->
            if (goal.id == goalId) goal.copy(savedAmount = goal.savedAmount + amount) else goal
        }
    }
}
