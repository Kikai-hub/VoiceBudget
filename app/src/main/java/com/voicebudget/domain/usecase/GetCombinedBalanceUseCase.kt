package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.model.TransferDirection
import com.voicebudget.domain.repository.ExchangeRateRepository
import com.voicebudget.domain.repository.TransactionRepository
import com.voicebudget.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class CombinedBalance(
    val amount: Double,
    val currency: Currency,
    /** True if one or more wallets couldn't be converted (no cached rate) and were left out of [amount]. */
    val hasUnavailableConversion: Boolean,
)

/**
 * All-time balance of every wallet, converted to the active wallet's currency and summed.
 * Reactive: recomputes whenever wallets, their transactions, or cached exchange rates change.
 */
class GetCombinedBalanceUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CombinedBalance?> =
        combine(walletRepository.observeWallets(), walletRepository.observeActiveWallet()) { wallets, active ->
            wallets to active
        }.flatMapLatest { (wallets, active) ->
            if (wallets.isEmpty() || active == null) return@flatMapLatest flowOf(null)
            val perWalletBalances = wallets.map { wallet ->
                transactionRepository.observeAllForWallet(wallet.id).map { transactions ->
                    wallet.currency to transactions.walletBalance()
                }
            }
            combine(perWalletBalances) { it.toList() }.combine(exchangeRateRepository.observeRates()) { balances, _ ->
                var total = 0.0
                var unavailable = false
                balances.forEach { (currency, amount) ->
                    val converted = exchangeRateRepository.convert(amount, currency, active.currency)
                    if (converted == null) unavailable = true else total += converted
                }
                CombinedBalance(total, active.currency, unavailable)
            }
        }

    private fun List<Transaction>.walletBalance(): Double = sumOf { transaction ->
        when (transaction.type) {
            TransactionType.INCOME -> transaction.amount
            TransactionType.EXPENSE -> -transaction.amount
            TransactionType.TRANSFER -> if (transaction.transferDirection == TransferDirection.IN) {
                transaction.amount
            } else {
                -transaction.amount
            }
        }
    }
}
