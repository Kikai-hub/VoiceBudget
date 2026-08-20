package com.voicebudget.fakes

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class FakeExchangeRateRepository(
    initialRates: Map<Currency, Double> = emptyMap(),
) : ExchangeRateRepository {

    private val rates = MutableStateFlow(initialRates)
    private val updatedAt = MutableStateFlow<Long?>(if (initialRates.isEmpty()) null else 0L)

    override fun observeRates() = rates

    override fun observeLastUpdatedAt() = updatedAt

    override suspend fun refreshRates(): Result<Unit> = Result.success(Unit)

    override suspend fun convert(amount: Double, from: Currency, to: Currency): Double? {
        if (from == to) return amount
        val current = rates.first()
        val fromRate = current[from] ?: return null
        val toRate = current[to] ?: return null
        return amount / fromRate * toRate
    }
}
