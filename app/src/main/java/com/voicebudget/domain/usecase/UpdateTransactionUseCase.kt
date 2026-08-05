package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val goalRepository: GoalRepository,
) {
    suspend operator fun invoke(transaction: Transaction) {
        val previous = repository.getById(transaction.id)
        repository.update(transaction)
        val goalId = transaction.goalId
        if (goalId != null && previous != null && previous.amount != transaction.amount) {
            goalRepository.contribute(goalId, transaction.amount - previous.amount)
        }
    }
}
