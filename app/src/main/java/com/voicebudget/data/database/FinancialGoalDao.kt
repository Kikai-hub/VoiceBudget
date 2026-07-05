package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialGoalDao {

    @Query("SELECT * FROM financial_goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FinancialGoalEntity>>

    @Insert
    suspend fun insert(goal: FinancialGoalEntity): Long

    @Delete
    suspend fun delete(goal: FinancialGoalEntity)
}
