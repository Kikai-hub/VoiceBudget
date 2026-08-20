package com.voicebudget.data.repository

import com.voicebudget.data.database.TransactionDao
import com.voicebudget.data.database.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory stand-in for Room's generated DAO, used to unit-test [TransactionRepositoryImpl] on the JVM. */
class FakeTransactionDao : TransactionDao {

    private val state = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllForWallet(walletId: Long) = state.map { list -> list.filter { it.walletId == walletId } }

    override suspend fun getAllForWalletOnce(walletId: Long): List<TransactionEntity> =
        state.value.filter { it.walletId == walletId }

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

    override suspend fun clearCustomCategory(categoryId: Long) {
        state.value = state.value.map { if (it.customCategoryId == categoryId) it.copy(customCategoryId = null) else it }
    }

    override suspend fun clearGoal(goalId: Long) {
        state.value = state.value.map { if (it.goalId == goalId) it.copy(goalId = null) else it }
    }

    override suspend fun getLastTransactionTime(): Long? = state.value.maxOfOrNull { it.createdAt }

    override suspend fun getCount(): Int = state.value.size
}
