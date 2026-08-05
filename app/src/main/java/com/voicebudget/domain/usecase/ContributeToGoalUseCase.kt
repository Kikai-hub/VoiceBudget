package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
import javax.inject.Inject

class ContributeToGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(goalId: Long, amount: Double) {
        require(amount > 0.0) { "Contribution amount must be positive" }
        transactionRunner.run {
            goalRepository.contribute(goalId, amount)
            val goal = goalRepository.getById(goalId)
            transactionRepository.add(
                Transaction(
                    amount = amount,
                    type = TransactionType.EXPENSE,
                    category = Category.SAVINGS,
                    description = goal?.name.orEmpty(),
                    createdAt = System.currentTimeMillis(),
                    goalId = goalId,
                ),
            )
        }
    }
}
