package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.model.TransferDirection
import com.voicebudget.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Records both legs of a transfer as a linked pair of [TransactionType.TRANSFER] transactions,
 * one per wallet, each in that wallet's own currency. Being a distinct transaction type (not a
 * flag like [com.voicebudget.domain.model.isGoalContribution]) means transfers are automatically
 * excluded from every existing income/expense sum without those call sites needing to know about
 * transfers at all.
 */
class TransferBetweenWalletsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        fromWalletId: Long,
        fromAmount: Double,
        outDescription: String,
        toWalletId: Long,
        toAmount: Double,
        inDescription: String,
    ) {
        val groupId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val out = Transaction(
            amount = fromAmount,
            type = TransactionType.TRANSFER,
            category = Category.TRANSFER,
            description = outDescription,
            createdAt = now,
            transferGroupId = groupId,
            transferDirection = TransferDirection.OUT,
        )
        val into = Transaction(
            amount = toAmount,
            type = TransactionType.TRANSFER,
            category = Category.TRANSFER,
            description = inDescription,
            createdAt = now,
            transferGroupId = groupId,
            transferDirection = TransferDirection.IN,
        )
        transactionRepository.addTransferPair(out, fromWalletId, into, toWalletId)
    }
}
