package com.voicebudget.domain.usecase

import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.fakes.FakeGoalRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.fakes.FakeTransactionRunner
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class DeleteTransactionUseCaseTest {

    @Test
    fun `deleting a goal-linked transaction reverses the goal's saved amount`() = runTest {
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

        DeleteTransactionUseCase(transactionRepo, goalRepo, FakeTransactionRunner())(contribution)

        assertEquals(0.0, goalRepo.getById(1)!!.savedAmount, 0.001)
    }

    @Test
    fun `deleting a transaction with no goal link does not touch any goal`() = runTest {
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

        DeleteTransactionUseCase(transactionRepo, goalRepo, FakeTransactionRunner())(expense)

        assertEquals(300.0, goalRepo.getById(1)!!.savedAmount, 0.001)
        assertNull(transactionRepo.current.value.find { it.id == 2L })
    }

    @Test
    fun `deleting the same goal-linked transaction twice only reverses the goal once`() = runTest {
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
        val useCase = DeleteTransactionUseCase(transactionRepo, goalRepo, FakeTransactionRunner())

        useCase(contribution)
        useCase(contribution)

        assertEquals(0.0, goalRepo.getById(1)!!.savedAmount, 0.001)
    }
}
