package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Wallet
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveWalletUseCase @Inject constructor(
    private val repository: WalletRepository,
) {
    operator fun invoke(): Flow<Wallet?> = repository.observeActiveWallet()
}
