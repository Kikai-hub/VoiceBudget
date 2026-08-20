package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.repository.ExchangeRateRepository
import javax.inject.Inject

class ConvertCurrencyUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    /** Null means no cached rate is available for one of the two currencies. */
    suspend operator fun invoke(amount: Double, from: Currency, to: Currency): Double? =
        repository.convert(amount, from, to)
}
