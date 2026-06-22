package com.voicebudget.presentation.settings

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.voicebudget.R
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.domain.usecase.AddTransactionUseCase
import com.voicebudget.domain.usecase.ClearAllDataUseCase
import com.voicebudget.domain.usecase.GetTransactionsUseCase
import com.voicebudget.domain.usecase.ObserveSettingsUseCase
import com.voicebudget.domain.usecase.UpdateCurrencyUseCase
import com.voicebudget.domain.usecase.UpdateRecognitionLanguageUseCase
import com.voicebudget.domain.usecase.UpdateThemeModeUseCase
import com.voicebudget.fakes.FakeSettingsRepository
import com.voicebudget.fakes.FakeTransactionRepository
import com.voicebudget.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun fakeContext(): Context {
        val context: Context = mockk(relaxed = true)
        every { context.getString(R.string.msg_all_data_cleared) } returns "All data cleared."
        every { context.getString(R.string.msg_export_success) } returns "Exported successfully."
        every { context.getString(R.string.msg_export_failed, any()) } answers { "Export failed: ${secondArg<Array<Any?>>()[0]}" }
        every { context.getString(R.string.msg_import_success, any()) } answers { "Imported ${secondArg<Array<Any?>>()[0]} transactions." }
        every { context.getString(R.string.msg_import_failed, any()) } answers { "Import failed: ${secondArg<Array<Any?>>()[0]}" }
        return context
    }

    private fun buildViewModel(
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        context: Context = fakeContext(),
    ): SettingsViewModel = SettingsViewModel(
        context,
        ObserveSettingsUseCase(settingsRepository),
        UpdateCurrencyUseCase(settingsRepository),
        UpdateThemeModeUseCase(settingsRepository),
        UpdateRecognitionLanguageUseCase(settingsRepository),
        ClearAllDataUseCase(transactionRepository),
        GetTransactionsUseCase(transactionRepository),
        AddTransactionUseCase(transactionRepository),
    )

    @Test
    fun `setCurrency updates settings state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()
            viewModel.setCurrency(Currency.USD)
            assertEquals(Currency.USD, awaitItem().settings.currency)
        }
    }

    @Test
    fun `setThemeMode updates settings state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()
            viewModel.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().settings.themeMode)
        }
    }

    @Test
    fun `clearAllData empties the transaction repository and reports a message`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val viewModel = buildViewModel(transactionRepository = transactionRepository)

        viewModel.message.test {
            assertEquals(null, awaitItem())
            viewModel.clearAllData()
            assertEquals("All data cleared.", awaitItem())
        }
    }

    @Test
    fun `export failure surfaces a message when the output stream cannot be opened`() = runTest {
        val context: Context = fakeContext()
        val uri: Uri = mockk(relaxed = true)
        every { context.contentResolver.openOutputStream(uri) } returns null
        val viewModel = buildViewModel(context = context)

        viewModel.message.test {
            assertEquals(null, awaitItem())
            viewModel.exportToCsv(uri)
            assertTrue(awaitItem()!!.startsWith("Export failed"))
        }
    }
}
