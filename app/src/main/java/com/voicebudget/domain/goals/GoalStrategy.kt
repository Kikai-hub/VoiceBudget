package com.voicebudget.domain.goals

data class GoalStrategy(
    val monthsRemaining: Int,
    val requiredMonthlySavings: Double,
    val currentMonthlySavings: Double,
    val onTrack: Boolean,
    val message: String,
    val suggestion: String?,
)

data class GoalWithStrategy(
    val goal: FinancialGoal,
    val strategy: GoalStrategy,
)
