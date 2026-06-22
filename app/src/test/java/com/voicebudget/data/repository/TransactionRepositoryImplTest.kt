package com.voicebudget.data.repository

import app.cash.turbine.test
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionRepositoryImplTest {

    private lateinit var dao: FakeTransactionDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeTransactionDao()
        repository = TransactionRepositoryImpl(dao)
    }

    private fun sampleTransaction(amount: Double = 350.0) = Transaction(
        amount = amount,
        type = TransactionType.EXPENSE,
        category = Category.CAFE,
        description = "Coffee",
        createdAt = 1_700_000_000_000L,
    )

    @Test
    fun `add maps domain model to entity and assigns an id`() = runTest {
        val id = repository.add(sampleTransaction())

        assertTrue(id > 0)
        repository.getAll().test {
            val transactions = awaitItem()
            assertEquals(1, transactions.size)
            assertEquals(id, transactions[0].id)
            assertEquals(350.0, transactions[0].amount, 0.0)
            assertEquals(Category.CAFE, transactions[0].category)
        }
    }

    @Test
    fun `update persists changes`() = runTest {
        val id = repository.add(sampleTransaction())
        val updated = sampleTransaction(amount = 500.0).copy(id = id)

        repository.update(updated)

        repository.getAll().test {
            assertEquals(500.0, awaitItem()[0].amount, 0.0)
        }
    }

    @Test
    fun `delete removes the transaction`() = runTest {
        val id = repository.add(sampleTransaction())
        val stored = sampleTransaction().copy(id = id)

        repository.delete(stored)

        repository.getAll().test {
            assertEquals(emptyList<Transaction>(), awaitItem())
        }
    }

    @Test
    fun `clearAll empties the store`() = runTest {
        repository.add(sampleTransaction())
        repository.add(sampleTransaction(amount = 850.0))

        repository.clearAll()

        repository.getAll().test {
            assertEquals(emptyList<Transaction>(), awaitItem())
        }
    }
}
