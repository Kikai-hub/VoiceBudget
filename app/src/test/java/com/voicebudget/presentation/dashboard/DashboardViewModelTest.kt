package com.voicebudget.presentation.dashboard

import app.cash.turbine.test
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.advisor.FinancialAdvisor
import com.voicebudget.domain.advisor.FinancialAnalyzer
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.goals.GoalAdvisor
import com.voicebudget.domain.goals.GoalStrategyBuilder
import com.voicebudget.domain.usecase.ContributeToGoalUseCase
import com.voicebudget.domain.usecase.GetCombinedBalanceUseCase
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.GetGoalsWithStrategyUseCase
import com.voicebudget.domain.usecase.GetMonthlySummaryUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveCustomCategoriesUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.ObserveWalletsUseCase
import com.voicebudget.fakes.FakeAdvisorSettingsRepository
import com.voicebudget.fakes.FakeCustomCategoryRepository
import com.voicebudget.fakes.FakeExchangeRateRepository
import com.voicebudget.fakes.FakeGoalRepository
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.fakes.FakeTransactionRunner
import com.voicebudget.fakes.FakeWalletRepository
import com.voicebudget.fakes.fakeAndroidContext
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
        val settingsRepo = FakeSettingsRepository()
        val goalRepo = FakeGoalRepository()
        val advisor = FinancialAdvisor(repository, advisorRepo, settingsRepo, FinancialAnalyzer(emptySet()))
        val goalAdvisor = GoalAdvisor(
            goalRepo,
            repository,
            settingsRepo,
            GoalStrategyBuilder(fakeAndroidContext(), MonthlyIncomeCalculator(), MonthlyExpenseCalculator(), CategoryAnalyzer()),
        )
        val walletRepo = FakeWalletRepository()
        val viewModel = DashboardViewModel(
            GetMonthlySummaryUseCase(repository),
            GetTransactionsUseCase(repository),
            GetFinancialAdviceUseCase(advisor),
            GetGoalsWithStrategyUseCase(goalAdvisor),
            ObserveSettingsUseCase(settingsRepo),
            ObserveCustomCategoriesUseCase(FakeCustomCategoryRepository()),
            GetCombinedBalanceUseCase(walletRepo, repository, FakeExchangeRateRepository()),
            ObserveWalletsUseCase(walletRepo),
            ContributeToGoalUseCase(goalRepo, repository, FakeTransactionRunner()),
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
        val settingsRepo = FakeSettingsRepository()
        val goalRepo = FakeGoalRepository()
        val advisor = FinancialAdvisor(emptyRepo, advisorRepo, settingsRepo, FinancialAnalyzer(emptySet()))
        val goalAdvisor = GoalAdvisor(
            goalRepo,
            emptyRepo,
            settingsRepo,
            GoalStrategyBuilder(fakeAndroidContext(), MonthlyIncomeCalculator(), MonthlyExpenseCalculator(), CategoryAnalyzer()),
        )
        val walletRepo = FakeWalletRepository()
        val viewModel = DashboardViewModel(
            GetMonthlySummaryUseCase(emptyRepo),
            GetTransactionsUseCase(emptyRepo),
            GetFinancialAdviceUseCase(advisor),
            GetGoalsWithStrategyUseCase(goalAdvisor),
            ObserveSettingsUseCase(settingsRepo),
            ObserveCustomCategoriesUseCase(FakeCustomCategoryRepository()),
            GetCombinedBalanceUseCase(walletRepo, emptyRepo, FakeExchangeRateRepository()),
            ObserveWalletsUseCase(walletRepo),
            ContributeToGoalUseCase(goalRepo, emptyRepo, FakeTransactionRunner()),
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
