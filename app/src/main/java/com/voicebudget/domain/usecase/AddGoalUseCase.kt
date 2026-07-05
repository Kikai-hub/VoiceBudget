package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import javax.inject.Inject

class AddGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goal: FinancialGoal): Long = repository.add(goal)
}
