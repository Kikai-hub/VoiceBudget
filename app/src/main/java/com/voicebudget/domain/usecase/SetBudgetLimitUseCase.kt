package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import javax.inject.Inject

class SetBudgetLimitUseCase @Inject constructor(
    private val repository: BudgetLimitRepository,
) {
    suspend operator fun invoke(limit: CategoryBudgetLimit): Long = repository.setLimit(limit)
}
