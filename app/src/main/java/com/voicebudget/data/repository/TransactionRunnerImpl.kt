package com.voicebudget.data.repository

import androidx.room.withTransaction
import com.voicebudget.data.database.AppDatabase
import com.voicebudget.domain.repository.TransactionRunner
import javax.inject.Inject

class TransactionRunnerImpl @Inject constructor(
    private val database: AppDatabase,
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = database.withTransaction(block)
}
