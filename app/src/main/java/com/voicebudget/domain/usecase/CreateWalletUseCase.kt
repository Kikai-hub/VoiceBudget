package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.Wallet
import com.voicebudget.domain.repository.WalletRepository
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val repository: WalletRepository,
) {
    suspend operator fun invoke(name: String, currency: Currency): Result<Wallet> =
        repository.createWallet(name, currency)
}
