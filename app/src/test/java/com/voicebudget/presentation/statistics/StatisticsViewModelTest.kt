package com.voicebudget.presentation.statistics

import app.cash.turbine.test
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.GetCategoryBreakdownUseCase
import com.voicebudget.domain.usecase.GetMonthlyTrendUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState aggregates expense breakdown and monthly trend for the current month`() = runTest {
        val now = System.currentTimeMillis()
        val repository = FakeTransactionRepository(
            listOf(
                Transaction(amount = 350.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = now),
                Transaction(amount = 150.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = now),
                Transaction(amount = 850.0, type = TransactionType.EXPENSE, category = Category.TRANSPORT, description = "Taxi", createdAt = now),
                Transaction(amount = 120000.0, type = TransactionType.INCOME, category = Category.SALARY, description = "Salary", createdAt = now),
            ),
        )
        val viewModel = StatisticsViewModel(
            GetCategoryBreakdownUseCase(repository),
            GetMonthlyTrendUseCase(repository),
            ObserveSettingsUseCase(FakeSettingsRepository()),
        )

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            val cafeTotal = state.categoryBreakdown.first { it.category == Category.CAFE }.amount
            assertEquals(500.0, cafeTotal, 0.0)
            val transportTotal = state.categoryBreakdown.first { it.category == Category.TRANSPORT }.amount
            assertEquals(850.0, transportTotal, 0.0)

            assertEquals(6, state.monthlyTrend.size)
            val currentMonth = state.monthlyTrend.last()
            assertEquals(120000.0, currentMonth.income, 0.0)
            assertEquals(1350.0, currentMonth.expense, 0.0)
        }
    }
}
