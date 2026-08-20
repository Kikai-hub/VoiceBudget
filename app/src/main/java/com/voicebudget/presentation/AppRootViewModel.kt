package com.voicebudget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicebudget.domain.usecase.EnsureDefaultWalletUseCase
import com.voicebudget.domain.usecase.MaterializeDueRecurringTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveExchangeRateLastUpdatedUseCase
import com.voicebudget.domain.usecase.ObserveWalletsUseCase
import com.voicebudget.domain.usecase.RefreshExchangeRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Gates the root UI between onboarding and the main app. Runs the silent default-wallet
 * migration for upgrading installs to completion *before* the wallets flow starts emitting,
 * so upgrading users never see a flash of onboarding while the migration is still running.
 */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val ensureDefaultWalletUseCase: EnsureDefaultWalletUseCase,
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val materializeDueRecurringTransactions: MaterializeDueRecurringTransactionsUseCase,
    private val refreshExchangeRates: RefreshExchangeRatesUseCase,
    private val observeExchangeRateLastUpdated: ObserveExchangeRateLastUpdatedUseCase,
) : ViewModel() {

    val hasWallet: StateFlow<Boolean?> = flow {
        ensureDefaultWalletUseCase()
        emitAll(observeWalletsUseCase().map { it.isNotEmpty() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Best-effort, run once per process: periodic WorkManager jobs aren't guaranteed to have
        // fired yet right after install/update, so catch up eagerly on cold start too.
        viewModelScope.launch {
            materializeDueRecurringTransactions()
            val lastUpdated = observeExchangeRateLastUpdated().first()
            val isStale = lastUpdated == null ||
                System.currentTimeMillis() - lastUpdated > TimeUnit.HOURS.toMillis(24)
            if (isStale) refreshExchangeRates()
        }
    }
}
