package com.voicebudget.fakes

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.MAX_WALLETS
import com.voicebudget.domain.model.Wallet
import com.voicebudget.domain.repository.WalletLimitReachedException
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class FakeWalletRepository(
    initialWallets: List<Wallet> = listOf(Wallet(id = 1, name = "Wallet 1", currency = Currency.RUB, createdAt = 0L)),
    initialActiveId: Long? = initialWallets.firstOrNull()?.id,
) : WalletRepository {

    private val wallets = MutableStateFlow(initialWallets)
    private val activeId = MutableStateFlow(initialActiveId)
    private var nextId = (initialWallets.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeWallets() = wallets

    override fun observeActiveWallet() = combine(wallets, activeId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }

    override fun observeActiveWalletId() = activeId

    override suspend fun getActiveWalletId(): Long? = observeActiveWallet().first()?.id

    override suspend fun getWalletById(id: Long): Wallet? = wallets.value.firstOrNull { it.id == id }

    override suspend fun createWallet(name: String, currency: Currency): Result<Wallet> {
        if (wallets.value.size >= MAX_WALLETS) return Result.failure(WalletLimitReachedException())
        val wallet = Wallet(id = nextId++, name = name, currency = currency, createdAt = 0L, orderIndex = wallets.value.size)
        wallets.value = wallets.value + wallet
        activeId.value = wallet.id
        return Result.success(wallet)
    }

    override suspend fun setActiveWallet(id: Long) {
        activeId.value = id
    }

    override suspend fun updateActiveWalletCurrency(currency: Currency) {
        val id = getActiveWalletId() ?: return
        wallets.value = wallets.value.map { if (it.id == id) it.copy(currency = currency) else it }
    }

    override suspend fun ensureDefaultWallet() {
        // No-op in tests: callers construct the initial wallet list explicitly.
    }
}
