package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateCurrencyUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(currency: Currency) = repository.setCurrency(currency)
}
