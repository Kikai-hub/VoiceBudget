package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("UPDATE transactions SET customCategoryId = NULL WHERE customCategoryId = :categoryId")
    suspend fun clearCustomCategory(categoryId: Long)

    @Query("UPDATE transactions SET goalId = NULL WHERE goalId = :goalId")
    suspend fun clearGoal(goalId: Long)

    @Query("SELECT MAX(createdAt) FROM transactions")
    suspend fun getLastTransactionTime(): Long?
}
