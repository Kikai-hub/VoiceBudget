package com.voicebudget.presentation.transactions

import app.cash.turbine.test
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.DeleteTransactionUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.UpdateTransactionUseCase
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = System.currentTimeMillis()
    private val coffee = Transaction(amount = 350.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = now)
    private val taxi = Transaction(amount = 850.0, type = TransactionType.EXPENSE, category = Category.TRANSPORT, description = "Taxi", createdAt = now)
    private val salary = Transaction(amount = 120000.0, type = TransactionType.INCOME, category = Category.SALARY, description = "Salary", createdAt = now)

    private fun buildViewModel(repository: FakeTransactionRepository): TransactionsViewModel = TransactionsViewModel(
        GetTransactionsUseCase(repository),
        ObserveSettingsUseCase(FakeSettingsRepository()),
        UpdateTransactionUseCase(repository),
        DeleteTransactionUseCase(repository),
    )

    @Test
    fun `unfiltered state contains all transactions`() = runTest {
        val viewModel = buildViewModel(FakeTransactionRepository(listOf(coffee, taxi, salary)))

        viewModel.uiState.test {
            skipItems(1)
            assertEquals(3, awaitItem().transactions.size)
        }
    }

    @Test
    fun `category filter narrows the list`() = runTest {
        val viewModel = buildViewModel(FakeTransactionRepository(listOf(coffee, taxi, salary)))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()
            viewModel.setCategoryFilter(Category.TRANSPORT)
            val filtered = awaitItem()
            assertEquals(1, filtered.transactions.size)
            assertEquals(taxi.description, filtered.transactions[0].description)
        }
    }

    @Test
    fun `date range filter excludes transactions outside the window`() = runTest {
        val lastMonth = Instant.now().atZone(ZoneId.systemDefault()).minusMonths(2).toInstant().toEpochMilli()
        val oldTransaction = coffee.copy(createdAt = lastMonth)
        val viewModel = buildViewModel(FakeTransactionRepository(listOf(oldTransaction, taxi)))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()
            viewModel.setDateRangeFilter(DateRangeFilter.THIS_MONTH)
            val filtered = awaitItem()
            assertEquals(1, filtered.transactions.size)
            assertEquals(taxi.description, filtered.transactions[0].description)
        }
    }

    @Test
    fun `saveEdit persists changes and clears editing state`() = runTest {
        val repository = FakeTransactionRepository(listOf(coffee))
        val viewModel = buildViewModel(repository)

        viewModel.uiState.test {
            skipItems(1)
            val initial = awaitItem()
            val stored = initial.transactions[0]

            viewModel.startEditing(stored)
            assertEquals(stored, awaitItem().editingTransaction)

            viewModel.saveEdit(stored.copy(amount = 400.0))
            val afterSave = awaitItem()
            assertEquals(null, afterSave.editingTransaction)
            assertEquals(400.0, afterSave.transactions[0].amount, 0.0)
        }
    }

    @Test
    fun `delete removes the transaction`() = runTest {
        val repository = FakeTransactionRepository(listOf(coffee, taxi))
        val viewModel = buildViewModel(repository)

        viewModel.uiState.test {
            skipItems(1)
            val initial = awaitItem()
            viewModel.delete(initial.transactions[0])
            assertEquals(1, awaitItem().transactions.size)
        }
    }
}
