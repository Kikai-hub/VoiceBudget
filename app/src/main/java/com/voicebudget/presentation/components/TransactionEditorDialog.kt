package com.voicebudget.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.voicebudget.R
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType

/**
 * Shared editable form for a transaction (amount/type/category/description), used both
 * when confirming a freshly parsed voice transaction and when editing an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorDialog(
    title: String,
    amountText: String,
    type: TransactionType,
    category: Category,
    description: String,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.action_save),
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TransactionType.entries.forEachIndexed { index, entryType ->
                        SegmentedButton(
                            selected = type == entryType,
                            onClick = { onTypeChange(entryType) },
                            shape = SegmentedButtonDefaults.itemShape(index, TransactionType.entries.size),
                        ) {
                            Text(stringResource(if (entryType == TransactionType.EXPENSE) R.string.type_expense else R.string.income))
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.field_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = categoryLabel(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.field_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                    ) {
                        Category.forType(type).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(categoryLabel(option)) },
                                onClick = {
                                    onCategoryChange(option)
                                    categoryMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
