package com.voicebudget.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import com.voicebudget.domain.goals.FinancialGoal

/**
 * Lets the user set aside a specific amount of money toward one of their goals. When more than
 * one goal exists, the user explicitly picks which one via a dropdown rather than the app
 * guessing — this is the "set aside" action requested to replace the old behavior where goal
 * progress was silently derived from the overall monthly balance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalContributionDialog(
    goals: List<FinancialGoal>,
    onConfirm: (goalId: Long, amount: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    if (goals.isEmpty()) return

    var selectedGoal by remember { mutableStateOf(goals.first()) }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var goalMenuExpanded by remember { mutableStateOf(false) }
    val invalidAmountError = stringResource(R.string.error_goal_contribution_invalid_amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goals_set_aside_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (goals.size > 1) {
                    ExposedDropdownMenuBox(
                        expanded = goalMenuExpanded,
                        onExpandedChange = { goalMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedGoal.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.goals_set_aside_field_goal)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = goalMenuExpanded,
                            onDismissRequest = { goalMenuExpanded = false },
                        ) {
                            goals.forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.name) },
                                    onClick = {
                                        selectedGoal = goal
                                        goalMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Text(selectedGoal.name, style = MaterialTheme.typography.titleSmall)
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.goals_set_aside_field_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(message) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.replace(',', '.').toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    error = invalidAmountError
                } else {
                    onConfirm(selectedGoal.id, amount)
                }
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
