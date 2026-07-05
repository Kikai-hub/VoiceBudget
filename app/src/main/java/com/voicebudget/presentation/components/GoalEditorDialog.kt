package com.voicebudget.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Form for creating a financial goal: name, target amount, and a target month/year picked
 * via two dropdowns (matching the Category dropdown pattern in [TransactionEditorDialog]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditorDialog(
    title: String,
    nameText: String,
    amountText: String,
    month: Month,
    year: Int,
    error: String?,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMonthChange: (Month) -> Unit,
    onYearChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }
    val years = remember { val currentYear = YearMonth.now().year; (currentYear..currentYear + 10).toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.goals_field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.goals_field_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(message) } },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ExposedDropdownMenuBox(
                        expanded = monthMenuExpanded,
                        onExpandedChange = { monthMenuExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.goals_field_month)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false },
                        ) {
                            Month.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                                    onClick = {
                                        onMonthChange(option)
                                        monthMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = yearMenuExpanded,
                        onExpandedChange = { yearMenuExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = year.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.goals_field_year)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false },
                        ) {
                            years.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.toString()) },
                                    onClick = {
                                        onYearChange(option)
                                        yearMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
