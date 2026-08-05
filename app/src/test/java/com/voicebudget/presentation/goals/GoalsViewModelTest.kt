package com.voicebudget.presentation.goals

import app.cash.turbine.test
import com.voicebudget.domain.advisor.calculators.CategoryAnalyzer
import com.voicebudget.domain.advisor.calculators.MonthlyExpenseCalculator
import com.voicebudget.domain.advisor.calculators.MonthlyIncomeCalculator
import com.voicebudget.domain.goals.GoalAdvisor
import com.voicebudget.domain.goals.GoalStrategyBuilder
import com.voicebudget.domain.usecase.AddGoalUseCase
import com.voicebudget.domain.usecase.ContributeToGoalUseCase
import com.voicebudget.domain.usecase.DeleteGoalUseCase
import com.voicebudget.domain.usecase.GetGoalsWithStrategyUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.fakes.FakeGoalRepository
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.fakes.FakeTransactionRunner
import com.voicebudget.fakes.fakeAndroidContext
import com.voicebudget.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(
        goalRepo: FakeGoalRepository = FakeGoalRepository(),
        transactionRepo: FakeTransactionRepository = FakeTransactionRepository(),
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
    ): GoalsViewModel {
        val strategyBuilder = GoalStrategyBuilder(
            fakeAndroidContext(),
            MonthlyIncomeCalculator(),
            MonthlyExpenseCalculator(),
            CategoryAnalyzer(),
        )
        val advisor = GoalAdvisor(goalRepo, transactionRepo, settingsRepo, strategyBuilder)
        return GoalsViewModel(
            context = fakeAndroidContext(),
            getGoalsWithStrategyUseCase = GetGoalsWithStrategyUseCase(advisor),
            addGoalUseCase = AddGoalUseCase(goalRepo),
            deleteGoalUseCase = DeleteGoalUseCase(goalRepo, transactionRepo, FakeTransactionRunner()),
            contributeToGoalUseCase = ContributeToGoalUseCase(goalRepo, transactionRepo, FakeTransactionRunner()),
            observeSettingsUseCase = ObserveSettingsUseCase(settingsRepo),
        )
    }

    @Test
    fun `initial state has no goals`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.goals.isEmpty())
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `dialog stays open with error when name is blank`() {
        val viewModel = buildViewModel()
        viewModel.openAddDialog()
        viewModel.updateDraft { it.copy(amountText = "1000") }
        viewModel.confirmAdd()

        val dialog = viewModel.dialogState.value as GoalDialogState.Editing
        assertNotNull(dialog.error)
    }

    @Test
    fun `dialog stays open with error when amount is not positive`() {
        val viewModel = buildViewModel()
        viewModel.openAddDialog()
        viewModel.updateDraft { it.copy(name = "Vacation", amountText = "0") }
        viewModel.confirmAdd()

        val dialog = viewModel.dialogState.value as GoalDialogState.Editing
        assertNotNull(dialog.error)
    }

    @Test
    fun `dialog stays open with error when deadline is in the past`() {
        val viewModel = buildViewModel()
        viewModel.openAddDialog()
        val past = YearMonth.now().minusMonths(1)
        viewModel.updateDraft {
            it.copy(name = "Vacation", amountText = "1000", month = past.month, year = past.year)
        }
        viewModel.confirmAdd()

        val dialog = viewModel.dialogState.value as GoalDialogState.Editing
        assertNotNull(dialog.error)
    }

    @Test
    fun `successful add closes the dialog and appears in uiState`() = runTest {
        val viewModel = buildViewModel()
        val future = YearMonth.now().plusMonths(2)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.openAddDialog()
            viewModel.updateDraft {
                it.copy(name = "Vacation", amountText = "1000", month = future.month, year = future.year)
            }
            viewModel.confirmAdd()

            val state = awaitItem()
            assertEquals(1, state.goals.size)
            assertEquals("Vacation", state.goals[0].goal.name)
        }

        assertEquals(GoalDialogState.Hidden, viewModel.dialogState.value)
    }

    @Test
    fun `delete removes the goal`() = runTest {
        val viewModel = buildViewModel()
        val future = YearMonth.now().plusMonths(2)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.openAddDialog()
            viewModel.updateDraft {
                it.copy(name = "Vacation", amountText = "1000", month = future.month, year = future.year)
            }
            viewModel.confirmAdd()
            val afterAdd = awaitItem()

            viewModel.deleteGoal(afterAdd.goals[0].goal)
            val afterDelete = awaitItem()
            assertTrue(afterDelete.goals.isEmpty())
        }
    }

    @Test
    fun `contributeToGoal increases the saved amount and progress`() = runTest {
        val viewModel = buildViewModel()
        val future = YearMonth.now().plusMonths(2)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.openAddDialog()
            viewModel.updateDraft {
                it.copy(name = "Vacation", amountText = "1000", month = future.month, year = future.year)
            }
            viewModel.confirmAdd()
            val afterAdd = awaitItem()

            viewModel.contributeToGoal(afterAdd.goals[0].goal.id, 250.0)
            val afterContribution = awaitItem()
            assertEquals(250.0, afterContribution.goals[0].strategy.savedAmount, 0.001)
            assertEquals(25, afterContribution.goals[0].strategy.progressPercent)
        }
    }
}
