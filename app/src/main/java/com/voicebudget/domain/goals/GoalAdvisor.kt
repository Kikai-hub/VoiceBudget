package com.voicebudget.domain.goals

import com.voicebudget.domain.repository.GoalRepository
import com.voicebudget.domain.repository.SettingsRepository
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Combines the live goal list with the transaction stream to produce a [GoalStrategy] for each
 * goal. [SettingsRepository.observeSettings] is included purely as a recompute trigger — its
 * value is unused — so switching the app language re-runs the analysis and re-localizes goal
 * strategy text immediately, mirroring [com.voicebudget.domain.advisor.FinancialAdvisor].
 */
class GoalAdvisor @Inject constructor(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    private val strategyBuilder: GoalStrategyBuilder,
) {
    operator fun invoke(): Flow<List<GoalWithStrategy>> =
        combine(
            goalRepository.observeAll(),
            transactionRepository.getAll(),
            settingsRepository.observeSettings(),
        ) { goals, transactions, _ ->
            goals.map { goal -> GoalWithStrategy(goal, strategyBuilder.build(goal, transactions)) }
        }.flowOn(Dispatchers.Default)
}
