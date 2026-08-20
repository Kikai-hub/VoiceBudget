package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class UpdateRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(recurring: RecurringTransaction) = repository.update(recurring)
}
