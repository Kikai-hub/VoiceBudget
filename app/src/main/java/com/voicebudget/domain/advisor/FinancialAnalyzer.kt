package com.voicebudget.domain.advisor

import javax.inject.Inject

/**
 * Orchestrates all registered [AdviceGenerator] instances.
 * Each generator is independent and contributes its own advice items;
 * results are merged and sorted by descending priority then creation time.
 */
class FinancialAnalyzer @Inject constructor(
    private val generators: @JvmSuppressWildcards Set<AdviceGenerator>,
) {
    fun analyze(context: AnalysisContext): List<FinancialAdvice> =
        generators
            .flatMap { it.generate(context) }
            .sortedWith(
                compareByDescending<FinancialAdvice> { it.priority.ordinal }
                    .thenByDescending { it.createdAt },
            )
}
