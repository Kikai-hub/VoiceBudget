package com.voicebudget.presentation.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voicebudget.domain.usecase.RefreshExchangeRatesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Daily best-effort refresh of currency exchange rates. Never fails the app if offline. */
@HiltWorker
class ExchangeRateRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refreshExchangeRates: RefreshExchangeRatesUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        refreshExchangeRates()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "exchange_rate_refresh"
    }
}
