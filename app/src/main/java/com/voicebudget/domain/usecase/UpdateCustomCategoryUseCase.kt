package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.repository.CustomCategoryRepository
import javax.inject.Inject

class UpdateCustomCategoryUseCase @Inject constructor(
    private val repository: CustomCategoryRepository,
) {
    suspend operator fun invoke(category: CustomCategory) = repository.update(category)
}
