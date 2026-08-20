package com.voicebudget.domain.model

data class Wallet(
    val id: Long = 0,
    val name: String,
    val currency: Currency,
    val createdAt: Long,
    val orderIndex: Int = 0,
)

const val MAX_WALLETS = 2
