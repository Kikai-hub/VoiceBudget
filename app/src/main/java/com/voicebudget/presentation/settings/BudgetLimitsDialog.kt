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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.usecase.BudgetLimitProgress
import com.voicebudget.presentation.components.categoryLabel

@Composable
fun BudgetLimitsHost(
    onDismiss: () -> Unit,
    viewModel: BudgetLimitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    BudgetLimitsListDialog(
        uiState = uiState,
        onAddClick = viewModel::openAddDialog,
        onDelete = viewModel::delete,
        onDismiss = onDismiss,
    )

    val adding = dialogState
    if (adding is BudgetLimitDialogState.Adding) {
        NewBudgetLimitDialog(
            state = adding,
            options = uiState.availableOptions,
            onOptionSelected = { option -> viewModel.updateDraft { it.copy(option = option) } },
            onAmountChange = { text -> viewModel.updateDraft { it.copy(amountText = text) } },
            onConfirm = viewModel::confirmAdd,
            onDismiss = viewModel::dismissDialog,
        )
    }
}

@Composable
private fun BudgetLimitsListDialog(
    uiState: BudgetLimitsUiState,
    onAddClick: () -> Unit,
    onDelete: (CategoryBudgetLimit) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.budget_limits_screen_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.progress.isEmpty()) {
                    Text(
                        text = stringResource(R.string.budget_limits_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                uiState.progress.forEach { progress ->
                    BudgetLimitRow(
                        progress = progress,
                        customCategories = uiState.customCategories,
                        onDelete = { onDelete(progress.limit) },
                    )
                }
                OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.budget_limits_add_button))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
private fun BudgetLimitRow(
    progress: BudgetLimitProgress,
    customCategories: List<com.voicebudget.domain.model.CustomCategory>,
    onDelete: () -> Unit,
) {
    val label = progress.limit.category?.let { categoryLabel(it) }
        ?: customCategories.firstOrNull { it.id == progress.limit.customCategoryId }?.name.orEmpty()
    val progressColor = when {
        progress.ratio >= 1.0 -> MaterialTheme.colorScheme.error
        progress.ratio >= 0.9 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label.ifBlank { stringResource(R.string.category_other) }, style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
            LinearProgressIndicator(
                progress = { progress.ratio.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = progressColor,
            )
            Text(
                text = "${"%.0f".format(progress.spent)} / ${"%.0f".format(progress.limit.monthlyLimit)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NewBudgetLimitDialog(
    state: BudgetLimitDialogState.Adding,
    options: List<BudgetCategoryOption>,
    onOptionSelected: (BudgetCategoryOption) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.budget_limit_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.budget_limit_field_category), style = MaterialTheme.typography.labelLarge)
                Column(
                    modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState()),
                ) {
                    options.forEach { option ->
                        val label = when (option) {
                            is BudgetCategoryOption.Builtin -> categoryLabel(option.category)
                            is BudgetCategoryOption.Custom -> option.customCategory.name
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
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.budget_limit_field_amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = state.option != null && (state.amountText.toDoubleOrNull() ?: 0.0) > 0.0,
            ) { Text(stringResource(R.string.budget_limit_save_button)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
