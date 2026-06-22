package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.TransactionRepository
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke() = repository.clearAll()
}
