package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import javax.inject.Inject

class CreateRecurringTransactionUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(recurring: RecurringTransaction): Long = repository.create(recurring)
}
