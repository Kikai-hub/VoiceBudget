package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.MonthlySummary
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMonthlySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(referenceTimeMillis: Long = System.currentTimeMillis()): Flow<MonthlySummary> {
        val referenceMonth = referenceTimeMillis.toYearMonth()
        return repository.getAll().map { transactions ->
            val monthly = transactions.filter { it.isInMonth(referenceMonth) }
            MonthlySummary(
                totalIncome = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                totalExpense = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }
    }
}
