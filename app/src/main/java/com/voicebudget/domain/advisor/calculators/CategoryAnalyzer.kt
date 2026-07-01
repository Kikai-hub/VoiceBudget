package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.advisor.toYearMonth
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.CategoryAmount
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

class CategoryAnalyzer @Inject constructor() {

    fun breakdownForMonth(
        transactions: List<Transaction>,
        month: YearMonth,
        type: TransactionType = TransactionType.EXPENSE,
    ): List<CategoryAmount> =
        transactions
            .filter { it.type == type && it.createdAt.toYearMonth() == month }
            .groupBy { it.category }
            .map { (category, items) -> CategoryAmount(category, items.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

    fun topCategory(transactions: List<Transaction>, month: YearMonth): CategoryAmount? =
        breakdownForMonth(transactions, month).firstOrNull()

    fun amountForCategory(
        transactions: List<Transaction>,
        category: Category,
        month: YearMonth,
    ): Double =
        transactions
            .filter { it.category == category && it.createdAt.toYearMonth() == month }
            .sumOf { it.amount }
}
