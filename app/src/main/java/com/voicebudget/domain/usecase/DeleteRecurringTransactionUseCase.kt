package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class DeleteRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(recurring: RecurringTransaction) = repository.delete(recurring)
}
