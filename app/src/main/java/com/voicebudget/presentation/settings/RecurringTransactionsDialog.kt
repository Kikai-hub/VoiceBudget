package com.voicebudget.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.RecurrenceFrequency
import com.voicebudget.domain.model.RecurringTransaction
import com.voicebudget.presentation.components.categoryLabel
import com.voicebudget.utils.formatAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionsHost(
    onDismiss: () -> Unit,
    viewModel: RecurringTransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    RecurringListDialog(
        uiState = uiState,
        onAddClick = viewModel::openAddDialog,
        onDelete = viewModel::delete,
        onDismiss = onDismiss,
    )

    val adding = dialogState
    if (adding is RecurringDialogState.Adding) {
        NewRecurringDialog(
            state = adding,
            options = uiState.availableOptions,
            onOptionSelected = { option -> viewModel.updateDraft { it.copy(option = option) } },
            onAmountChange = { text -> viewModel.updateDraft { it.copy(amountText = text) } },
            onDescriptionChange = { text -> viewModel.updateDraft { it.copy(description = text) } },
            onFrequencyChange = { frequency -> viewModel.updateDraft { it.copy(frequency = frequency) } },
            onConfirm = viewModel::confirmAdd,
            onDismiss = viewModel::dismissDialog,
        )
    }
}

@Composable
private fun RecurringListDialog(
    uiState: RecurringTransactionsUiState,
    onAddClick: () -> Unit,
    onDelete: (RecurringTransaction) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recurring_screen_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.rules.isEmpty()) {
                    Text(
                        text = stringResource(R.string.recurring_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                uiState.rules.forEach { rule ->
                    RecurringRow(rule = rule, customCategoryName = uiState.customCategories.firstOrNull { it.id == rule.customCategoryId }?.name, onDelete = { onDelete(rule) })
                }
                OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.recurring_add_button))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
private fun RecurringRow(rule: RecurringTransaction, customCategoryName: String?, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = rule.description.ifBlank { customCategoryName ?: categoryLabel(rule.category) },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${formatAmount(rule.amount, "")} · ${frequencyLabel(rule.frequency)} · " +
                        stringResource(R.string.recurring_next_run, formatDate(rule.nextRunAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun frequencyLabel(frequency: RecurrenceFrequency): String = stringResource(
    when (frequency) {
        RecurrenceFrequency.DAILY -> R.string.recurring_frequency_daily
        RecurrenceFrequency.WEEKLY -> R.string.recurring_frequency_weekly
        RecurrenceFrequency.MONTHLY -> R.string.recurring_frequency_monthly
    },
)

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))

@Composable
private fun NewRecurringDialog(
    state: RecurringDialogState.Adding,
    options: List<RecurringCategoryOption>,
    onOptionSelected: (RecurringCategoryOption) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFrequencyChange: (RecurrenceFrequency) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recurring_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.recurring_field_description)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.recurring_field_amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.recurring_field_category), style = MaterialTheme.typography.labelLarge)
                Column(modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        val label = when (option) {
                            is RecurringCategoryOption.Builtin -> categoryLabel(option.category)
                            is RecurringCategoryOption.Custom -> option.customCategory.name
                        }
                        val isSelected = option == state.option
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                                .padding(vertical = 8.dp),
                        )
                    }
                }
                Text(stringResource(R.string.recurring_field_frequency), style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    RecurrenceFrequency.entries.forEachIndexed { index, frequency ->
                        SegmentedButton(
                            selected = state.frequency == frequency,
                            onClick = { onFrequencyChange(frequency) },
                            shape = SegmentedButtonDefaults.itemShape(index, RecurrenceFrequency.entries.size),
                        ) {
                            Text(frequencyLabel(frequency))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = state.option != null && (state.amountText.toDoubleOrNull() ?: 0.0) > 0.0,
            ) { Text(stringResource(R.string.recurring_save_button)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
