package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import javax.inject.Inject

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goal: FinancialGoal) = repository.delete(goal)
}
