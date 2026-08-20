package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface BudgetLimitAlert {
    val limit: CategoryBudgetLimit
    val spent: Double

    data class Approaching(override val limit: CategoryBudgetLimit, override val spent: Double) : BudgetLimitAlert
    data class Exceeded(override val limit: CategoryBudgetLimit, override val spent: Double) : BudgetLimitAlert
}

/**
 * Compares month-to-date spend against every budget limit (across all wallets) and returns the
 * alerts newly due this call — i.e. not already raised for this month/threshold — while marking
 * them notified so the daily worker and the post-transaction check don't double-notify.
 */
class CheckBudgetLimitsUseCase @Inject constructor(
    private val budgetLimitRepository: BudgetLimitRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(referenceTimeMillis: Long = System.currentTimeMillis()): List<BudgetLimitAlert> {
        val referenceMonth = referenceTimeMillis.toYearMonth()
        val monthKey = referenceMonth.toString()
        val limits = budgetLimitRepository.observeAll().first()
        val alerts = mutableListOf<BudgetLimitAlert>()

        for (limit in limits) {
            if (limit.monthlyLimit <= 0) continue
            val transactions = transactionRepository.getAllForWallet(limit.walletId)
            val spent = transactions.spentAgainst(limit, referenceMonth)
            val ratio = spent / limit.monthlyLimit
            val threshold = when {
                ratio >= 1.0 -> 100
                ratio >= APPROACHING_RATIO -> 90
                else -> null
            } ?: continue

            val alreadyNotified = limit.lastNotifiedMonth == monthKey && (limit.lastNotifiedThreshold ?: 0) >= threshold
            if (alreadyNotified) continue

            budgetLimitRepository.markNotified(limit.id, monthKey, threshold)
            alerts += if (threshold >= 100) {
                BudgetLimitAlert.Exceeded(limit, spent)
            } else {
                BudgetLimitAlert.Approaching(limit, spent)
            }
        }
        return alerts
    }

    private companion object {
        const val APPROACHING_RATIO = 0.9
    }
}
