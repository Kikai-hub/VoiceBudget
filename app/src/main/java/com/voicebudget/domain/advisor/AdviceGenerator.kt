package com.voicebudget.domain.advisor

/** Single-responsibility contract implemented by every advice generator. */
interface AdviceGenerator {
    fun generate(context: AnalysisContext): List<FinancialAdvice>
}
