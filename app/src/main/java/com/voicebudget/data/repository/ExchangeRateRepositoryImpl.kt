package com.voicebudget.data.repository

import com.voicebudget.data.database.ExchangeRateDao
import com.voicebudget.data.database.ExchangeRateEntity
import com.voicebudget.data.remote.ExchangeRateApiClient
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val dao: ExchangeRateDao,
    private val apiClient: ExchangeRateApiClient,
) : ExchangeRateRepository {

    override fun observeRates(): Flow<Map<Currency, Double>> =
        dao.getAll().map { entities ->
            entities.mapNotNull { entity ->
                runCatching { Currency.valueOf(entity.currencyCode) }.getOrNull()
                    ?.let { currency -> currency to entity.rateToUsd }
            }.toMap()
        }

    override fun observeLastUpdatedAt(): Flow<Long?> =
        dao.getAll().map { entities -> entities.minOfOrNull { it.updatedAt } }

    override suspend fun refreshRates(): Result<Unit> {
        val rates = apiClient.fetchRatesToUsd().getOrElse { return Result.failure(it) }
        val now = System.currentTimeMillis()
        val entities = Currency.entries.mapNotNull { currency ->
            rates[currency.name]?.let { rate -> ExchangeRateEntity(currency.name, rate, now) }
        }
        if (entities.isEmpty()) return Result.failure(IllegalStateException("No known currencies in API response"))
        dao.upsertAll(entities)
        return Result.success(Unit)
    }

    override suspend fun convert(amount: Double, from: Currency, to: Currency): Double? {
        if (from == to) return amount
        val rates = observeRates().first()
        val fromRate = rates[from] ?: return null
        val toRate = rates[to] ?: return null
        return amount / fromRate * toRate
    }
}
