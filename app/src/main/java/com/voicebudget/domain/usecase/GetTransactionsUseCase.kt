package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.getAll()
}
