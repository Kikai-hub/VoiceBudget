package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecurringTransactionsUseCase @Inject constructor(
    private val repository: RecurringTransactionRepository,
) {
    operator fun invoke(): Flow<List<RecurringTransaction>> = repository.observeForActiveWallet()
}
