package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(transaction: Transaction) {
        transactionRunner.run {
            // Guards against a double-delete (e.g. a rapid double-tap on the confirm button)
            // decrementing the linked goal's savedAmount twice for a transaction that only
            // existed once.
            val existing = repository.getById(transaction.id) ?: return@run
            repository.delete(existing)
            existing.goalId?.let { goalId -> goalRepository.contribute(goalId, -existing.amount) }
        }
    }
}
