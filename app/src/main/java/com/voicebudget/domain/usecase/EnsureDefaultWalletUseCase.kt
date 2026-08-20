package com.voicebudget.domain.usecase

import com.voicebudget.domain.repository.WalletRepository
import javax.inject.Inject

class EnsureDefaultWalletUseCase @Inject constructor(
    private val repository: WalletRepository,
) {
    suspend operator fun invoke() = repository.ensureDefaultWallet()
}
