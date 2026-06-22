package com.voicebudget.fakes

import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTransactionRepository(initial: List<Transaction> = emptyList()) : TransactionRepository {

    private var nextId = 1L

    private val state = MutableStateFlow(
        initial
            .map { if (it.id == 0L) it.copy(id = nextId++) else it }
            .sortedByDescending { it.createdAt },
    )
    val current: StateFlow<List<Transaction>> = state

    override fun getAll() = state

    override suspend fun add(transaction: Transaction): Long {
        val withId = if (transaction.id == 0L) transaction.copy(id = nextId++) else transaction
        state.value = (state.value + withId).sortedByDescending { it.createdAt }
        return withId.id
    }

    override suspend fun update(transaction: Transaction) {
        state.value = state.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun delete(transaction: Transaction) {
        state.value = state.value.filterNot { it.id == transaction.id }
    }

    override suspend fun clearAll() {
        state.value = emptyList()
    }
}
