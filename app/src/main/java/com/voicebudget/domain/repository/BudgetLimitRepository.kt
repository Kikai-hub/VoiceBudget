package com.voicebudget.domain.repository

import com.voicebudget.domain.model.CategoryBudgetLimit
import kotlinx.coroutines.flow.Flow

interface BudgetLimitRepository {
    fun observeForActiveWallet(): Flow<List<CategoryBudgetLimit>>
    fun observeAll(): Flow<List<CategoryBudgetLimit>>
    suspend fun setLimit(limit: CategoryBudgetLimit): Long
    suspend fun delete(limit: CategoryBudgetLimit)
    suspend fun markNotified(id: Long, month: String, threshold: Int)
}
