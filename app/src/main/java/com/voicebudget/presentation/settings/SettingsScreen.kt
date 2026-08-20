package com.voicebudget.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.AppSettings
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.presentation.components.CurrencyPickerList
import com.voicebudget.presentation.components.LanguagePickerList
import com.voicebudget.presentation.components.languageOptions
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import com.voicebudget.utils.currencyLabel
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
        onRefreshExchangeRates = viewModel::refreshExchangeRates,
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
    onRefreshExchangeRates: () -> Unit,
    onConsumeMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showWalletManagement by remember { mutableStateOf(false) }
    var showBudgetLimits by remember { mutableStateOf(false) }
    var showRecurringTransactions by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection {
            SettingsRowButton(
                label = stringResource(R.string.settings_wallets),
                onClick = { showWalletManagement = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_language),
                value = languageOptions.firstOrNull { it.first == uiState.settings.recognitionLanguageTag }?.second,
                onClick = { showLanguagePicker = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_currency),
                value = currencyLabel(uiState.settings.currency),
                onClick = { showCurrencyPicker = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_theme),
                value = stringResource(
                    when (uiState.settings.themeMode) {
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                        ThemeMode.SYSTEM -> R.string.theme_system
                    },
                ),
                onClick = { showThemePicker = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_exchange_rate),
                value = if (uiState.isRefreshingRates) {
                    stringResource(R.string.settings_exchange_rate_refresh) + "…"
                } else {
                    uiState.exchangeRateUpdatedAt?.let { stringResource(R.string.settings_exchange_rate_updated, formatRelativeTime(it)) }
                        ?: stringResource(R.string.settings_exchange_rate_never)
                },
                onClick = onRefreshExchangeRates,
                showDivider = false,
            )
        }

        SettingsSection {
            SettingsRowButton(
                label = stringResource(R.string.settings_categories),
                onClick = { showCategoryManagement = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_budget_limits),
                onClick = { showBudgetLimits = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_recurring_transactions),
                onClick = { showRecurringTransactions = true },
            )
            SettingsRowButton(
                label = stringResource(R.string.settings_security),
                onClick = { showSecuritySettings = true },
                showDivider = false,
            )
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

    if (showCategoryManagement) {
        CategoryManagementHost(onDismiss = { showCategoryManagement = false })
    }

    if (showWalletManagement) {
        WalletManagementHost(onDismiss = { showWalletManagement = false })
    }

    if (showBudgetLimits) {
        BudgetLimitsHost(onDismiss = { showBudgetLimits = false })
    }

    if (showRecurringTransactions) {
        RecurringTransactionsHost(onDismiss = { showRecurringTransactions = false })
    }

    if (showSecuritySettings) {
        SecuritySettingsHost(onDismiss = { showSecuritySettings = false })
    }

    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                LanguagePickerList(
                    selectedTag = uiState.settings.recognitionLanguageTag,
                    onSelected = {
                        onSetRecognitionLanguage(it)
                        showLanguagePicker = false
                    },
                    modifier = Modifier.heightIn(max = 420.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) { Text(stringResource(R.string.action_close)) }
            },
        )
    }

    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            title = { Text(stringResource(R.string.settings_currency)) },
            text = {
                CurrencyPickerList(
                    selected = uiState.settings.currency,
                    onSelected = {
                        onSetCurrency(it)
                        showCurrencyPicker = false
                    },
                    modifier = Modifier.heightIn(max = 420.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyPicker = false }) { Text(stringResource(R.string.action_close)) }
            },
        )
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text(stringResource(R.string.settings_theme)) },
            text = {
                ThemeSelector(
                    selected = uiState.settings.themeMode,
                    onSelected = {
                        onSetThemeMode(it)
                        showThemePicker = false
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) { Text(stringResource(R.string.action_close)) }
            },
        )
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
private fun SettingsSection(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun SettingsRowButton(
    label: String,
    onClick: () -> Unit,
    value: String? = null,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider()
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
            onRefreshExchangeRates = {},
            onConsumeMessage = {},
        )
    }
}

private fun formatRelativeTime(epochMillis: Long): String {
    val minutes = (System.currentTimeMillis() - epochMillis) / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m"
        minutes < 24 * 60 -> "${minutes / 60}h"
        else -> "${minutes / (24 * 60)}d"
    }
}
