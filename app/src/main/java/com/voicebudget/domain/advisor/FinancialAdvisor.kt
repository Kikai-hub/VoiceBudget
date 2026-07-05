package com.voicebudget.domain.advisor

import com.voicebudget.domain.repository.AdvisorSettingsRepository
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.YearMonth
import javax.inject.Inject

/**
 * Main entry point for the Financial Advisor feature.
 * Combines the live transaction stream, advisor settings, dismissed IDs, and the app
 * language into a reactive [Flow] of [FinancialAdvice] items — the language is included
 * purely as a recompute trigger so advice text re-renders in the newly selected locale.
 * Analysis runs on [Dispatchers.Default] so the UI thread is never blocked.
 */
class FinancialAdvisor @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val advisorSettingsRepository: AdvisorSettingsRepository,
    private val settingsRepository: SettingsRepository,
    private val analyzer: FinancialAnalyzer,
) {
    operator fun invoke(): Flow<List<FinancialAdvice>> =
        combine(
            transactionRepository.getAll(),
            advisorSettingsRepository.observeSettings(),
            advisorSettingsRepository.observeDismissedIds(),
            settingsRepository.observeSettings(),
        ) { transactions, settings, dismissedIds, _ ->
            val context = AnalysisContext(
                allTransactions = transactions,
                currentMonth = YearMonth.now(),
                settings = settings,
                dismissedIds = dismissedIds,
            )
            analyzer.analyze(context)
        }.flowOn(Dispatchers.Default)
}
