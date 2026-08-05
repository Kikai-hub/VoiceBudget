package com.voicebudget.data.repository

import com.voicebudget.data.database.CustomCategoryDao
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomCategoryRepositoryImpl @Inject constructor(
    private val dao: CustomCategoryDao,
) : CustomCategoryRepository {

    override fun observeAll(): Flow<List<CustomCategory>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(category: CustomCategory): Long = dao.insert(category.toEntity())

    override suspend fun update(category: CustomCategory) = dao.update(category.toEntity())

    override suspend fun delete(category: CustomCategory) = dao.delete(category.toEntity())
}
