package com.voicebudget

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.voicebudget.domain.usecase.GetCombinedBalanceUseCase
import com.voicebudget.presentation.notifications.ExchangeRateRefreshWorker
import com.voicebudget.presentation.notifications.RecurringTransactionWorker
import com.voicebudget.presentation.notifications.TransactionReminderWorker
import com.voicebudget.presentation.security.AppLockState
import com.voicebudget.presentation.widget.BalanceGlanceWidget
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class VoiceBudgetApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLockState: AppLockState

    @Inject
    lateinit var getCombinedBalanceUseCase: GetCombinedBalanceUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLockState)
        scheduleReminderWork()
        scheduleExchangeRateRefreshWork()
        scheduleRecurringTransactionWork()
        observeBalanceForWidget()
    }

    /**
     * Keeps the home-screen widget's balance fresh reactively — whatever changes it (voice/manual
     * entry, transfers, recurring materialization, CSV import, goal contributions, rate refresh)
     * only needs to change the underlying data; this is the single place that notices and repaints
     * the widget, rather than every write site remembering to call it.
     */
    private fun observeBalanceForWidget() {
        applicationScope.launch {
            getCombinedBalanceUseCase().collect {
                BalanceGlanceWidget.refreshAll(this@VoiceBudgetApp)
            }
        }
    }

    private fun scheduleReminderWork() {
        val request = PeriodicWorkRequestBuilder<TransactionReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TransactionReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun scheduleExchangeRateRefreshWork() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = PeriodicWorkRequestBuilder<ExchangeRateRefreshWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ExchangeRateRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun scheduleRecurringTransactionWork() {
        val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
