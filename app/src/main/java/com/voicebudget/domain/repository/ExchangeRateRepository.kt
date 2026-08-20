package com.voicebudget.domain.repository

import com.voicebudget.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {
    fun observeRates(): Flow<Map<Currency, Double>>
    fun observeLastUpdatedAt(): Flow<Long?>

    /** Fetches fresh rates from the network and caches them. Fails silently-safe: never throws. */
    suspend fun refreshRates(): Result<Unit>

    /** Converts [amount] from [from] to [to] using the last cached rates. Null if a rate is missing. */
    suspend fun convert(amount: Double, from: Currency, to: Currency): Double?
}
