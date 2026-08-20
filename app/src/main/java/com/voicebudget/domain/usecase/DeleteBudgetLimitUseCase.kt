package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.repository.BudgetLimitRepository
import javax.inject.Inject

class DeleteBudgetLimitUseCase @Inject constructor(
    private val repository: BudgetLimitRepository,
) {
    suspend operator fun invoke(limit: CategoryBudgetLimit) = repository.delete(limit)
}
