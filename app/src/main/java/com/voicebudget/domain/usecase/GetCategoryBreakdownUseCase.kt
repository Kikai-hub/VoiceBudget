package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryAmount
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(
        type: TransactionType = TransactionType.EXPENSE,
        referenceTimeMillis: Long = System.currentTimeMillis(),
    ): Flow<List<CategoryAmount>> {
        val referenceMonth = referenceTimeMillis.toYearMonth()
        return repository.getAll().map { transactions ->
            transactions
                .filter { it.type == type && it.isInMonth(referenceMonth) }
                .groupBy { it.category }
                .map { (category, items) -> CategoryAmount(category, items.sumOf { it.amount }) }
                .sortedByDescending { it.amount }
        }
    }
}
