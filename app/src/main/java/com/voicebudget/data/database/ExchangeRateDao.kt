package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates")
    fun getAll(): Flow<List<ExchangeRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rates: List<ExchangeRateEntity>)

    @Query("SELECT MIN(updatedAt) FROM exchange_rates")
    suspend fun getOldestUpdatedAt(): Long?
}
