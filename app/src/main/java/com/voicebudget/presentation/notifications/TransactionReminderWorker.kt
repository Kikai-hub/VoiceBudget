package com.voicebudget.presentation.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voicebudget.domain.notifications.InactivityReminderCalculator
import com.voicebudget.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Daily check that nudges the user when they haven't logged a transaction in a while. */
@HiltWorker
class TransactionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val reminderCalculator: InactivityReminderCalculator,
    private val notifier: ReminderNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lastTransactionAt = transactionRepository.getLastTransactionTime()
        if (reminderCalculator.shouldRemind(lastTransactionAt)) {
            notifier.showInactivityReminder()
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "transaction_reminder"
    }
}
