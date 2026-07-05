package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.GoalAdvisor
import com.voicebudget.domain.goals.GoalWithStrategy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGoalsWithStrategyUseCase @Inject constructor(
    private val advisor: GoalAdvisor,
) {
    operator fun invoke(): Flow<List<GoalWithStrategy>> = advisor()
}
