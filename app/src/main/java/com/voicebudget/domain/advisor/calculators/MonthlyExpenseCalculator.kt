package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.advisor.toYearMonth
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

class MonthlyExpenseCalculator @Inject constructor() {

    fun forMonth(transactions: List<Transaction>, month: YearMonth): Double =
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.createdAt.toYearMonth() == month }
            .sumOf { it.amount }

    fun byMonth(transactions: List<Transaction>): Map<YearMonth, Double> =
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.createdAt.toYearMonth() }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
}
