package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.RecurrenceFrequency
import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.RecurringTransactionRepository
import com.voicebudget.domain.repository.TransactionRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Turns every due recurring rule into real transactions, catching up on missed occurrences
 * (e.g. the app wasn't opened for a while) up to [MAX_CATCH_UP_OCCURRENCES] per rule so a
 * long-neglected rule can't generate an unbounded backlog.
 */
class MaterializeDueRecurringTransactionsUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
        recurringTransactionRepository.getDue(nowMillis).forEach { rule -> materializeOne(rule, nowMillis) }
    }

    private suspend fun materializeOne(rule: RecurringTransaction, nowMillis: Long) {
        var nextRunAt = rule.nextRunAt
        var occurrences = 0
        while (nextRunAt <= nowMillis && occurrences < MAX_CATCH_UP_OCCURRENCES) {
            transactionRepository.addToWallet(
                Transaction(
                    amount = rule.amount,
                    type = rule.type,
                    category = rule.category,
                    customCategoryId = rule.customCategoryId,
                    description = rule.description,
                    createdAt = nextRunAt,
                ),
                rule.walletId,
            )
            nextRunAt = advance(nextRunAt, rule.frequency)
            occurrences++
        }
        val stillActive = rule.active && (rule.endDate == null || nextRunAt <= rule.endDate)
        recurringTransactionRepository.update(rule.copy(nextRunAt = nextRunAt, active = stillActive))
    }

    private fun advance(fromMillis: Long, frequency: RecurrenceFrequency): Long {
        val zoned = Instant.ofEpochMilli(fromMillis).atZone(ZoneId.systemDefault())
        val next = when (frequency) {
            RecurrenceFrequency.DAILY -> zoned.plusDays(1)
            RecurrenceFrequency.WEEKLY -> zoned.plusWeeks(1)
            RecurrenceFrequency.MONTHLY -> zoned.plusMonths(1)
        }
        return next.toInstant().toEpochMilli()
    }

    private companion object {
        const val MAX_CATCH_UP_OCCURRENCES = 36
    }
}
