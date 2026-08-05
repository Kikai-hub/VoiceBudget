package com.voicebudget.fakes

import com.voicebudget.domain.repository.TransactionRunner

class FakeTransactionRunner : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}
