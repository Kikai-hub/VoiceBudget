package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.ExchangeRateRepository
import javax.inject.Inject

class RefreshExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshRates()
}
