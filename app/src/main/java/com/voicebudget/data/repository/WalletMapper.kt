package com.voicebudget.data.repository

import com.voicebudget.data.database.WalletEntity
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.Wallet

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    currency = runCatching { Currency.valueOf(currency) }.getOrDefault(Currency.USD),
    createdAt = createdAt,
    orderIndex = orderIndex,
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    currency = currency.name,
    createdAt = createdAt,
    orderIndex = orderIndex,
)
