package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.fakes.FakeGoalRepository
import com.voicebudget.fakes.FakeTransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class UpdateTransactionUseCaseTest {

    @Test
    fun `changing the amount of a goal-linked transaction adjusts the goal by the delta`() = runTest {
        val goalRepo = FakeGoalRepository(
            listOf(FinancialGoal(id = 1, name = "Car", targetAmount = 1000.0, targetMonth = YearMonth.now(), createdAt = 0, savedAmount = 300.0)),
        )
        val contribution = Transaction(
            id = 1,
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = Category.SAVINGS,
            description = "Car",
            createdAt = System.currentTimeMillis(),
            goalId = 1,
        )
        val transactionRepo = FakeTransactionRepository(listOf(contribution))

        UpdateTransactionUseCase(transactionRepo, goalRepo)(contribution.copy(amount = 500.0))

        assertEquals(500.0, goalRepo.getById(1)!!.savedAmount, 0.001)
    }

    @Test
    fun `editing a transaction with no goal link does not touch any goal`() = runTest {
        val goalRepo = FakeGoalRepository(
            listOf(FinancialGoal(id = 1, name = "Car", targetAmount = 1000.0, targetMonth = YearMonth.now(), createdAt = 0, savedAmount = 300.0)),
        )
        val expense = Transaction(
            id = 2,
            amount = 500.0,
            type = TransactionType.EXPENSE,
            category = Category.FOOD,
            description = "Groceries",
            createdAt = System.currentTimeMillis(),
        )
        val transactionRepo = FakeTransactionRepository(listOf(expense))

        UpdateTransactionUseCase(transactionRepo, goalRepo)(expense.copy(amount = 800.0))

        assertEquals(300.0, goalRepo.getById(1)!!.savedAmount, 0.001)
    }
}
