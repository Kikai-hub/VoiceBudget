package com.voicebudget.presentation.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voicebudget.domain.usecase.MaterializeDueRecurringTransactionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Daily catch-up that turns due recurring rules (subscriptions, rent, salary...) into real transactions. */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val materializeDueRecurringTransactions: MaterializeDueRecurringTransactionsUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        materializeDueRecurringTransactions()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "recurring_transaction_materialize"
    }
}
