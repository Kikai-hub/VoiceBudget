package com.voicebudget.presentation.voice

import android.content.Context
import app.cash.turbine.test
import com.voicebudget.R
import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.parser.TransactionParser
import com.voicebudget.domain.usecase.AddTransactionUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.ParseVoiceInputUseCase
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun fakeContext(): Context {
        val context: Context = mockk(relaxed = true)
        every { context.getString(R.string.error_invalid_amount) } returns "Please enter a valid amount."
        every { context.getString(R.string.error_no_speech_match) } returns "Didn't catch that. Please try again."
        every { context.getString(R.string.error_amount_not_found, any()) } answers {
            "Couldn't find an amount in \"${secondArg<Array<Any?>>()[0]}\". Please try again."
        }
        return context
    }

    private fun buildViewModel(
        events: List<RecognitionEvent>,
        repository: FakeTransactionRepository = FakeTransactionRepository(),
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
    ): Pair<AddTransactionViewModel, FakeVoiceRecognizerService> {
        val voiceService = FakeVoiceRecognizerService(events)
        val viewModel = AddTransactionViewModel(
            fakeContext(),
            voiceService,
            ParseVoiceInputUseCase(TransactionParser()),
            AddTransactionUseCase(repository),
            ObserveSettingsUseCase(settingsRepository),
        )
        return viewModel to voiceService
    }

    @Test
    fun `recognized speech moves to Confirming with parsed draft`() = runTest {
        val (viewModel, _) = buildViewModel(listOf(RecognitionEvent.FinalResult("Taxi 850")))

        viewModel.uiState.test {
            assertEquals(AddTransactionUiState.Idle, awaitItem())
            viewModel.startListening()
            assertEquals(AddTransactionUiState.Listening, awaitItem())
            val confirming = awaitItem() as AddTransactionUiState.Confirming
            assertEquals(TransactionType.EXPENSE, confirming.draft.type)
            assertEquals(Category.TRANSPORT, confirming.draft.category)
            assertEquals("850", confirming.draft.amountText)
            assertEquals("Taxi", confirming.draft.description)
        }
    }

    @Test
    fun `recognition error surfaces as Error state`() = runTest {
        val (viewModel, _) = buildViewModel(listOf(RecognitionEvent.Error("No match")))

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.startListening()
            awaitItem() // Listening
            val error = awaitItem() as AddTransactionUiState.Error
            assertEquals("No match", error.message)
        }
    }

    @Test
    fun `input without an amount surfaces a friendly error`() = runTest {
        val (viewModel, _) = buildViewModel(listOf(RecognitionEvent.FinalResult("Coffee")))

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.startListening()
            awaitItem() // Listening
            val error = awaitItem() as AddTransactionUiState.Error
            assertTrue(error.message.contains("Coffee"))
        }
    }

    @Test
    fun `confirm saves the transaction and moves to Saved`() = runTest {
        val repository = FakeTransactionRepository()
        val (viewModel, _) = buildViewModel(listOf(RecognitionEvent.FinalResult("Coffee 350")), repository = repository)

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.startListening()
            awaitItem() // Listening
            awaitItem() // Confirming

            viewModel.confirm()
            awaitItem() // Saving
            assertEquals(AddTransactionUiState.Saved, awaitItem())
        }

        assertEquals(1, repository.current.value.size)
        assertEquals(350.0, repository.current.value[0].amount, 0.0)
    }

    @Test
    fun `confirm with a blank amount surfaces an error instead of saving`() = runTest {
        val repository = FakeTransactionRepository()
        val (viewModel, _) = buildViewModel(listOf(RecognitionEvent.FinalResult("Coffee 350")), repository = repository)

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.startListening()
            awaitItem() // Listening
            awaitItem() // Confirming

            viewModel.updateDraft { it.copy(amountText = "") }
            awaitItem() // Confirming with updated draft

            viewModel.confirm()
            val error = awaitItem() as AddTransactionUiState.Error
            assertTrue(error.message.contains("valid amount"))
        }
        assertEquals(0, repository.current.value.size)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `startListening defaults to the configured recognition language`() = runTest {
        val (viewModel, voiceService) = buildViewModel(
            listOf(RecognitionEvent.FinalResult("Coffee 350")),
            settingsRepository = FakeSettingsRepository(AppSettings(recognitionLanguageTag = "en-US")),
        )
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.startListening()
            awaitItem() // Listening
            awaitItem() // Confirming
        }

        assertEquals("en-US", voiceService.lastLanguageTag)
    }
}
