package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets ORDER BY orderIndex ASC, id ASC")
    fun getAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WalletEntity?

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)
}
