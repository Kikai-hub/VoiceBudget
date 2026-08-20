package com.voicebudget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val currencyCode: String,
    val rateToUsd: Double,
    val updatedAt: Long,
)
