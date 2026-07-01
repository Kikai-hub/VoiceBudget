package com.voicebudget.domain.usecase

import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.FinancialAdvisor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFinancialAdviceUseCase @Inject constructor(
    private val advisor: FinancialAdvisor,
) {
    operator fun invoke(): Flow<List<FinancialAdvice>> = advisor()
}
