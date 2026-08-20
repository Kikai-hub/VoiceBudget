package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBudgetLimitsUseCase @Inject constructor(
    private val repository: BudgetLimitRepository,
) {
    operator fun invoke(): Flow<List<CategoryBudgetLimit>> = repository.observeForActiveWallet()
}
