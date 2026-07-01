package com.voicebudget.domain.advisor.calculators

import com.voicebudget.domain.advisor.toYearMonth
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject

class SmallPurchaseAnalyzer @Inject constructor() {

    fun countForMonth(
        transactions: List<Transaction>,
        month: YearMonth,
        threshold: Double,
    ): Int =
        transactions.count { tx ->
            tx.type == TransactionType.EXPENSE &&
                tx.createdAt.toYearMonth() == month &&
                tx.amount < threshold
        }
}
