package com.voicebudget.fakes

import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCustomCategoryRepository(
    initial: List<CustomCategory> = emptyList(),
) : CustomCategoryRepository {

    private val categoriesFlow = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeAll(): Flow<List<CustomCategory>> = categoriesFlow

    override suspend fun add(category: CustomCategory): Long {
        val id = nextId++
        categoriesFlow.value = categoriesFlow.value + category.copy(id = id)
        return id
    }

    override suspend fun update(category: CustomCategory) {
        categoriesFlow.value = categoriesFlow.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun delete(category: CustomCategory) {
        categoriesFlow.value = categoriesFlow.value.filterNot { it.id == category.id }
    }
}
