package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction) = repository.delete(transaction)
}
