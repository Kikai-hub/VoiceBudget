package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExchangeRateLastUpdatedUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastUpdatedAt()
}
