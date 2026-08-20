package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE walletId = :walletId ORDER BY nextRunAt ASC")
    fun getAllForWallet(walletId: Long): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE active = 1 AND nextRunAt <= :nowMillis")
    suspend fun getDue(nowMillis: Long): List<RecurringTransactionEntity>

    @Insert
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)
}
