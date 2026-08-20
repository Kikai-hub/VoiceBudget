package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialGoalDao {

    @Query("SELECT * FROM financial_goals WHERE walletId = :walletId ORDER BY createdAt DESC")
    fun getAllForWallet(walletId: Long): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE id = :goalId LIMIT 1")
    suspend fun getById(goalId: Long): FinancialGoalEntity?

    @Insert
    suspend fun insert(goal: FinancialGoalEntity): Long

    @Delete
    suspend fun delete(goal: FinancialGoalEntity)

    @Query("UPDATE financial_goals SET savedAmount = savedAmount + :amount WHERE id = :goalId")
    suspend fun contribute(goalId: Long, amount: Double)

    @Query("SELECT COUNT(*) FROM financial_goals")
    suspend fun getCount(): Int
}
