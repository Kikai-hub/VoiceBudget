package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
import javax.inject.Inject

/**
 * Deleting a goal detaches it from any transactions that reference it (they keep their amount
 * and category but stop counting toward a now-nonexistent goal) instead of leaving a dangling
 * foreign key that would silently no-op future contribute/delete adjustments.
 */
class DeleteGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(goal: FinancialGoal) {
        transactionRunner.run {
            transactionRepository.clearGoal(goal.id)
            goalRepository.delete(goal)
        }
    }
}
