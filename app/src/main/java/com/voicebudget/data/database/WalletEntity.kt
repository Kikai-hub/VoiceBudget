package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val currency: String,
    val createdAt: Long,
    val orderIndex: Int = 0,
)
