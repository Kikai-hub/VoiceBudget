package com.voicebudget.domain.repository

/** Runs a block of repository calls atomically, so multi-write use cases can't leave partial state. */
interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
