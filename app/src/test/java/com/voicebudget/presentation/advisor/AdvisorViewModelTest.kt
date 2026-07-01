package com.voicebudget.presentation.advisor

import app.cash.turbine.test
import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.AnalysisContext
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.domain.advisor.FinancialAdvisor
import com.voicebudget.domain.advisor.FinancialAnalyzer
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.usecase.DismissAdviceUseCase
import com.voicebudget.domain.usecase.GetAdvisorSettingsUseCase
import com.voicebudget.domain.usecase.GetFinancialAdviceUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.fakes.FakeAdvisorSettingsRepository
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class AdvisorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun epochMillis(year: Int, month: Int): Long =
        YearMonth.of(year, month).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun buildViewModel(
        transactions: List<Transaction> = emptyList(),
        advisorSettings: AdvisorSettings = AdvisorSettings(),
    ): AdvisorViewModel {
        val transactionRepo = FakeTransactionRepository(transactions)
        val advisorRepo = FakeAdvisorSettingsRepository(advisorSettings)
        val settingsRepo = FakeSettingsRepository()

        val analyzer = FinancialAnalyzer(emptySet())
        val advisor = FinancialAdvisor(transactionRepo, advisorRepo, analyzer)

        return AdvisorViewModel(
            getFinancialAdviceUseCase = GetFinancialAdviceUseCase(advisor),
            getAdvisorSettingsUseCase = GetAdvisorSettingsUseCase(advisorRepo),
            observeSettingsUseCase = ObserveSettingsUseCase(settingsRepo),
            dismissAdviceUseCase = DismissAdviceUseCase(advisorRepo),
            updateAdvisorSettingsUseCase = com.voicebudget.domain.usecase.UpdateAdvisorSettingsUseCase(advisorRepo),
        )
    }

    @Test
    fun `initial state is loading`() {
        val viewModel = buildViewModel()
        assertEquals(true, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `state transitions from loading to ready`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiState.test {
            skipItems(1) // loading
            val state = awaitItem()
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `advice list is empty when no generators produce advice`() = runTest {
        val viewModel = buildViewModel(transactions = emptyList())
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.advice.isEmpty())
        }
    }

    @Test
    fun `currency symbol comes from app settings`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals("₽", state.currencySymbol)
        }
    }

    @Test
    fun `advisor settings are reflected in state`() = runTest {
        val settings = AdvisorSettings(topCategoryThresholdPercent = 45.0)
        val viewModel = buildViewModel(advisorSettings = settings)
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(45.0, state.settings.topCategoryThresholdPercent, 0.001)
        }
    }
}
