package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetLimitDao {

    @Query("SELECT * FROM category_budget_limits WHERE walletId = :walletId")
    fun getAllForWallet(walletId: Long): Flow<List<CategoryBudgetLimitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(limit: CategoryBudgetLimitEntity): Long

    @Update
    suspend fun update(limit: CategoryBudgetLimitEntity)

    @Delete
    suspend fun delete(limit: CategoryBudgetLimitEntity)

    @Query(
        "UPDATE category_budget_limits SET lastNotifiedMonth = :month, lastNotifiedThreshold = :threshold WHERE id = :id",
    )
    suspend fun markNotified(id: Long, month: String, threshold: Int)
}
