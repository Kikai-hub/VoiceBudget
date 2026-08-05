package com.voicebudget.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.goals.FinancialGoal
import com.voicebudget.domain.goals.GoalWithStrategy
import com.voicebudget.presentation.components.GoalContributionDialog
import com.voicebudget.presentation.components.GoalEditorDialog
import com.voicebudget.utils.formatAmount

@Composable
fun GoalsSection(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    var pendingDelete by remember { mutableStateOf<FinancialGoal?>(null) }
    var showContributionDialog by remember { mutableStateOf(false) }

    GoalsSectionContent(
        uiState = uiState,
        onAddClick = viewModel::openAddDialog,
        onSetAsideClick = { showContributionDialog = true },
        onDeleteClick = { pendingDelete = it },
        modifier = modifier,
    )

    if (showContributionDialog) {
        GoalContributionDialog(
            goals = uiState.goals.map { it.goal },
            onConfirm = { goalId, amount ->
                viewModel.contributeToGoal(goalId, amount)
                showContributionDialog = false
            },
            onDismiss = { showContributionDialog = false },
        )
    }

    val dialog = dialogState
    if (dialog is GoalDialogState.Editing) {
        GoalEditorDialog(
            title = stringResource(R.string.goals_dialog_add_title),
            nameText = dialog.name,
            amountText = dialog.amountText,
            month = dialog.month,
            year = dialog.year,
            error = dialog.error,
            onNameChange = { name -> viewModel.updateDraft { it.copy(name = name) } },
            onAmountChange = { amount -> viewModel.updateDraft { it.copy(amountText = amount) } },
            onMonthChange = { month -> viewModel.updateDraft { it.copy(month = month) } },
            onYearChange = { year -> viewModel.updateDraft { it.copy(year = year) } },
            onConfirm = viewModel::confirmAdd,
            onDismiss = viewModel::dismissDialog,
        )
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.goals_delete_title)) },
            text = { Text(stringResource(R.string.goals_delete_message, goal.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun GoalsSectionContent(
    uiState: GoalsUiState,
    onAddClick: () -> Unit,
    onSetAsideClick: () -> Unit,
    onDeleteClick: (FinancialGoal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.goals_title),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.goals.isNotEmpty()) {
                    TextButton(onClick = onSetAsideClick) {
                        Icon(Icons.Filled.Savings, contentDescription = null)
                        Text(stringResource(R.string.goals_set_aside))
                    }
                }
                TextButton(onClick = onAddClick) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.goals_add_goal))
                }
            }

            if (uiState.goals.isEmpty()) {
                Text(
                    text = stringResource(R.string.goals_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    uiState.goals.forEach { item ->
                        GoalCard(
                            item = item,
                            currencySymbol = uiState.currencySymbol,
                            onDelete = { onDeleteClick(item.goal) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    item: GoalWithStrategy,
    currencySymbol: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.goal.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        formatAmount(item.goal.targetAmount, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(
                        when {
                            item.strategy.progressPercent >= 100 -> R.string.goals_completed
                            item.strategy.onTrack -> R.string.goals_on_track
                            else -> R.string.goals_off_track
                        },
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.strategy.onTrack) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_delete))
                }
            }

            LinearProgressIndicator(
                progress = { item.strategy.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            Text(
                text = stringResource(
                    R.string.goals_saved_of_target,
                    formatAmount(item.strategy.savedAmount, currencySymbol),
                    formatAmount(item.goal.targetAmount, currencySymbol),
                    item.strategy.progressPercent,
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.goals_months_remaining, item.strategy.monthsRemaining),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(
                    R.string.goals_required_monthly,
                    formatAmount(item.strategy.requiredMonthlySavings, currencySymbol),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(
                    R.string.goals_current_monthly,
                    formatAmount(item.strategy.currentMonthlySavings, currencySymbol),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = item.strategy.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            item.strategy.suggestion?.let { suggestion ->
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
