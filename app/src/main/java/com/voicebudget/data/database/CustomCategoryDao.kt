package com.voicebudget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAll(): Flow<List<CustomCategoryEntity>>

    @Insert
    suspend fun insert(category: CustomCategoryEntity): Long

    @Update
    suspend fun update(category: CustomCategoryEntity)

    @Delete
    suspend fun delete(category: CustomCategoryEntity)
}
