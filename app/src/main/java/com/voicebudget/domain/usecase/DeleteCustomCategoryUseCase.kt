package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.repository.CustomCategoryRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.TransactionRunner
import javax.inject.Inject

/**
 * Deleting a custom category detaches it from any transactions that reference it (they fall
 * back to displaying their built-in bucket category) instead of leaving a dangling foreign key.
 */
class DeleteCustomCategoryUseCase @Inject constructor(
    private val categoryRepository: CustomCategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(category: CustomCategory) {
        transactionRunner.run {
            transactionRepository.clearCustomCategory(category.id)
            categoryRepository.delete(category)
        }
    }
}
