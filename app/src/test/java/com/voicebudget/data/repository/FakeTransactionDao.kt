package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionDao
import com.voicebudget.data.database.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory stand-in for Room's generated DAO, used to unit-test [TransactionRepositoryImpl] on the JVM. */
class FakeTransactionDao : TransactionDao {

    private val state = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private var nextId = 1L

    override fun getAll() = state

    override suspend fun getById(id: Long): TransactionEntity? = state.value.find { it.id == id }

    override suspend fun insert(transaction: TransactionEntity): Long {
        val withId = transaction.copy(id = nextId++)
        state.value = (state.value + withId).sortedByDescending { it.createdAt }
        return withId.id
    }

    override suspend fun update(transaction: TransactionEntity) {
        state.value = state.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun delete(transaction: TransactionEntity) {
        state.value = state.value.filterNot { it.id == transaction.id }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}
