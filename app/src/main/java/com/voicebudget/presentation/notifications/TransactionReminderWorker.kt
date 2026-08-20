package com.voicebudget.presentation.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voicebudget.domain.notifications.InactivityReminderCalculator
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.usecase.BudgetLimitAlert
import com.voicebudget.domain.usecase.CheckBudgetLimitsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Daily check that (a) nudges the user when they haven't logged a transaction in a while, and
 * (b) sweeps every category budget limit for month-to-date overruns, per [CheckBudgetLimitsUseCase].
 */
@HiltWorker
class TransactionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val reminderCalculator: InactivityReminderCalculator,
    private val notifier: ReminderNotifier,
    private val checkBudgetLimitsUseCase: CheckBudgetLimitsUseCase,
    private val budgetLimitAlertPresenter: BudgetLimitAlertPresenter,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lastTransactionAt = transactionRepository.getLastTransactionTime()
        if (reminderCalculator.shouldRemind(lastTransactionAt)) {
            notifier.showInactivityReminder()
        }

        checkBudgetLimitsUseCase().forEach { alert ->
            val (categoryLabel, spentText, limitText) = budgetLimitAlertPresenter.present(alert)
            when (alert) {
                is BudgetLimitAlert.Approaching -> notifier.showBudgetLimitApproaching(categoryLabel, spentText, limitText)
                is BudgetLimitAlert.Exceeded -> notifier.showBudgetLimitExceeded(categoryLabel, spentText, limitText)
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "transaction_reminder"
    }
}
