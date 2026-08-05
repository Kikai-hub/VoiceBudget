package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCustomCategoriesUseCase @Inject constructor(
    private val repository: CustomCategoryRepository,
) {
    operator fun invoke(): Flow<List<CustomCategory>> = repository.observeAll()
}
