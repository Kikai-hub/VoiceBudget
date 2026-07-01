package com.voicebudget.presentation.dashboard

import app.cash.turbine.test
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.advisor.FinancialAdvisor
import com.voicebudget.domain.advisor.FinancialAnalyzer
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.GetMonthlySummaryUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.fakes.FakeAdvisorSettingsRepository
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState reflects monthly summary, recent transactions and currency`() = runTest {
        val now = System.currentTimeMillis()
        val repository = FakeTransactionRepository(
            listOf(
                Transaction(amount = 1000.0, type = TransactionType.INCOME, category = Category.SALARY, description = "Salary", createdAt = now),
                Transaction(amount = 200.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = now),
            ),
        )
        val advisorRepo = FakeAdvisorSettingsRepository()
        val advisor = FinancialAdvisor(repository, advisorRepo, FinancialAnalyzer(emptySet()))
        val viewModel = DashboardViewModel(
            GetMonthlySummaryUseCase(repository),
            GetTransactionsUseCase(repository),
            GetFinancialAdviceUseCase(advisor),
            ObserveSettingsUseCase(FakeSettingsRepository()),
        )

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(1000.0, state.summary.totalIncome, 0.0)
            assertEquals(200.0, state.summary.totalExpense, 0.0)
            assertEquals(2, state.recentTransactions.size)
            assertEquals("₽", state.currencySymbol)
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `empty repository yields zeroed summary and empty list`() = runTest {
        val emptyRepo = FakeTransactionRepository()
        val advisorRepo = FakeAdvisorSettingsRepository()
        val advisor = FinancialAdvisor(emptyRepo, advisorRepo, FinancialAnalyzer(emptySet()))
        val viewModel = DashboardViewModel(
            GetMonthlySummaryUseCase(emptyRepo),
            GetTransactionsUseCase(emptyRepo),
            GetFinancialAdviceUseCase(advisor),
            ObserveSettingsUseCase(FakeSettingsRepository()),
        )

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(0.0, state.summary.totalIncome, 0.0)
            assertEquals(0.0, state.summary.totalExpense, 0.0)
            assertEquals(emptyList<Transaction>(), state.recentTransactions)
        }
    }
}
