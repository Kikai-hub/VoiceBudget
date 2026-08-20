package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class BudgetLimitProgress(
    val limit: CategoryBudgetLimit,
    val spent: Double,
) {
    val ratio: Double get() = if (limit.monthlyLimit <= 0) 0.0 else spent / limit.monthlyLimit
}

/** Current-month spend per category limit, for the active wallet. */
class GetBudgetLimitProgressUseCase @Inject constructor(
    private val budgetLimitRepository: BudgetLimitRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(referenceTimeMillis: Long = System.currentTimeMillis()): Flow<List<BudgetLimitProgress>> {
        val referenceMonth = referenceTimeMillis.toYearMonth()
        return combine(budgetLimitRepository.observeForActiveWallet(), transactionRepository.getAll()) { limits, transactions ->
            limits.map { limit -> BudgetLimitProgress(limit, transactions.spentAgainst(limit, referenceMonth)) }
        }
    }
}
