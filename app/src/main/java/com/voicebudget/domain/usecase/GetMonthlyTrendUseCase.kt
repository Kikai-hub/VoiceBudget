package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.MonthlyTrendPoint
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMonthlyTrendUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(
        monthsBack: Int = 6,
        referenceTimeMillis: Long = System.currentTimeMillis(),
    ): Flow<List<MonthlyTrendPoint>> {
        val referenceMonth = referenceTimeMillis.toYearMonth()
        val months = (monthsBack - 1 downTo 0).map { referenceMonth.minusMonths(it.toLong()) }

        return repository.getAll().map { transactions ->
            months.map { month ->
                val monthly = transactions.filter { it.isInMonth(month) }
                MonthlyTrendPoint(
                    yearMonth = month,
                    income = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                )
            }
        }
    }
}
