package com.voicebudget.domain.repository

import com.voicebudget.domain.model.CustomCategory
import kotlinx.coroutines.flow.Flow

interface CustomCategoryRepository {
    fun observeAll(): Flow<List<CustomCategory>>
    suspend fun add(category: CustomCategory): Long
    suspend fun update(category: CustomCategory)
    suspend fun delete(category: CustomCategory)
}
