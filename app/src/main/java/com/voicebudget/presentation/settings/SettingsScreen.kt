package com.voicebudget.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let(viewModel::exportToCsv)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importFromCsv)
    }

    SettingsContent(
        uiState = uiState,
        message = message,
        onSetCurrency = viewModel::setCurrency,
        onSetRecognitionLanguage = viewModel::setRecognitionLanguage,
        onSetThemeMode = viewModel::setThemeMode,
        onExportClick = { exportLauncher.launch("transactions.csv") },
        onImportClick = { importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
        onClearAllData = viewModel::clearAllData,
        onConsumeMessage = viewModel::consumeMessage,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    message: String?,
    onSetCurrency: (Currency) -> Unit,
    onSetRecognitionLanguage: (String) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onClearAllData: () -> Unit,
    onConsumeMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection(title = stringResource(R.string.settings_currency)) {
            CurrencySelector(selected = uiState.settings.currency, onSelected = onSetCurrency)
        }

        SettingsSection(title = stringResource(R.string.settings_language)) {
            LanguageSelector(
                selectedTag = uiState.settings.recognitionLanguageTag,
                onSelected = onSetRecognitionLanguage,
            )
        }

        SettingsSection(title = stringResource(R.string.settings_theme)) {
            ThemeSelector(selected = uiState.settings.themeMode, onSelected = onSetThemeMode)
        }

        SettingsSection(title = stringResource(R.string.settings_data)) {
            Button(onClick = onExportClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings_export_csv)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings_import_csv)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showClearConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.settings_clear_all_data)) }
        }
    }

    message?.let { text ->
        LaunchedEffect(text) {
            delay(3000)
            onConsumeMessage()
        }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text(text) }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.dialog_clear_all_data_title)) },
            text = { Text(stringResource(R.string.dialog_clear_all_data_message)) },
            confirmButton = {
                Button(onClick = {
                    onClearAllData()
                    showClearConfirm = false
                }) { Text(stringResource(R.string.action_clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun CurrencySelector(selected: Currency, onSelected: (Currency) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row {
        OutlinedButton(onClick = { expanded = true }) { Text("${selected.name} (${selected.symbol})") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${currency.name} (${currency.symbol})") },
                    onClick = {
                        onSelected(currency)
                        expanded = false
                    },
                )
            }
        }
    }
}

private val languageOptions = listOf("ru-RU" to "Русский", "en-US" to "English")

@Composable
private fun LanguageSelector(selectedTag: String, onSelected: (String) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        languageOptions.forEachIndexed { index, (tag, label) ->
            SegmentedButton(
                selected = selectedTag == tag,
                onClick = { onSelected(tag) },
                shape = SegmentedButtonDefaults.itemShape(index, languageOptions.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
            ) {
                Text(
                    stringResource(
                        when (mode) {
                            ThemeMode.LIGHT -> R.string.theme_light
                            ThemeMode.DARK -> R.string.theme_dark
                            ThemeMode.SYSTEM -> R.string.theme_system
                        },
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    VoiceBudgetTheme {
        SettingsContent(
            uiState = SettingsUiState(isLoading = false, settings = AppSettings()),
            message = null,
            onSetCurrency = {},
            onSetRecognitionLanguage = {},
            onSetThemeMode = {},
            onExportClick = {},
            onImportClick = {},
            onClearAllData = {},
            onConsumeMessage = {},
        )
    }
}
