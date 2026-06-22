package com.voicebudget.presentation.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.presentation.components.EmptyState
import com.voicebudget.presentation.components.TransactionEditorDialog
import com.voicebudget.presentation.components.TransactionItem
import com.voicebudget.presentation.components.categoryLabel
import com.voicebudget.presentation.theme.VoiceBudgetTheme

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    TransactionsContent(
        uiState = uiState,
        onCategorySelected = viewModel::setCategoryFilter,
        onDateRangeSelected = viewModel::setDateRangeFilter,
        onStartEditing = viewModel::startEditing,
        onSaveEdit = viewModel::saveEdit,
        onCancelEditing = viewModel::cancelEditing,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
private fun TransactionsContent(
    uiState: TransactionsUiState,
    onCategorySelected: (Category?) -> Unit,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    onStartEditing: (Transaction) -> Unit,
    onSaveEdit: (Transaction) -> Unit,
    onCancelEditing: () -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        FilterRow(
            filters = uiState.filters,
            onCategorySelected = onCategorySelected,
            onDateRangeSelected = onDateRangeSelected,
        )

        if (uiState.transactions.isEmpty()) {
            EmptyState(message = stringResource(R.string.transactions_empty_filtered))
        } else {
            LazyColumn {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = uiState.currencySymbol,
                        onClick = onStartEditing,
                        onDelete = { pendingDelete = it },
                    )
                }
            }
        }
    }

    uiState.editingTransaction?.let { editing ->
        EditTransactionDialog(
            transaction = editing,
            onSave = onSaveEdit,
            onDismiss = onCancelEditing,
        )
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_transaction_title)) },
            text = { Text(stringResource(R.string.dialog_delete_transaction_message, transaction.description)) },
            confirmButton = {
                Button(onClick = {
                    onDelete(transaction)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun FilterRow(
    filters: TransactionFilters,
    onCategorySelected: (Category?) -> Unit,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var dateMenuExpanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box {
            FilterChip(
                selected = filters.category != null,
                onClick = { categoryMenuExpanded = true },
                label = { Text(filters.category?.let { categoryLabel(it) } ?: stringResource(R.string.category_all)) },
            )
            DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.category_all)) }, onClick = {
                    onCategorySelected(null)
                    categoryMenuExpanded = false
                })
                Category.entries.forEach { category ->
                    DropdownMenuItem(text = { Text(categoryLabel(category)) }, onClick = {
                        onCategorySelected(category)
                        categoryMenuExpanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            FilterChip(
                selected = filters.dateRange != DateRangeFilter.ALL_TIME,
                onClick = { dateMenuExpanded = true },
                label = { Text(dateRangeLabel(filters.dateRange)) },
            )
            DropdownMenu(expanded = dateMenuExpanded, onDismissRequest = { dateMenuExpanded = false }) {
                DateRangeFilter.entries.forEach { range ->
                    DropdownMenuItem(text = { Text(dateRangeLabel(range)) }, onClick = {
                        onDateRangeSelected(range)
                        dateMenuExpanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun dateRangeLabel(range: DateRangeFilter): String = stringResource(
    when (range) {
        DateRangeFilter.ALL_TIME -> R.string.date_range_all_time
        DateRangeFilter.THIS_MONTH -> R.string.this_month
        DateRangeFilter.LAST_MONTH -> R.string.date_range_last_month
        DateRangeFilter.LAST_7_DAYS -> R.string.date_range_last_7_days
    },
)

@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    onSave: (Transaction) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember(transaction.id) { mutableStateOf(transaction.amount.toPlainString()) }
    var type by remember(transaction.id) { mutableStateOf(transaction.type) }
    var category by remember(transaction.id) { mutableStateOf(transaction.category) }
    var description by remember(transaction.id) { mutableStateOf(transaction.description) }

    TransactionEditorDialog(
        title = stringResource(R.string.edit_transaction_title),
        amountText = amountText,
        type = type,
        category = category,
        description = description,
        onAmountChange = { amountText = it },
        onTypeChange = { newType ->
            type = newType
            category = Category.other(newType)
        },
        onCategoryChange = { category = it },
        onDescriptionChange = { description = it },
        onConfirm = {
            val amount = amountText.replace(',', '.').toDoubleOrNull()
            if (amount != null && amount > 0.0) {
                onSave(transaction.copy(amount = amount, type = type, category = category, description = description))
            }
        },
        onDismiss = onDismiss,
    )
}

private fun Double.toPlainString(): String =
    if (this == Math.floor(this)) toLong().toString() else toString()

private val sampleTransactionsList = listOf(
    Transaction(id = 1, amount = 350.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = System.currentTimeMillis()),
    Transaction(id = 2, amount = 850.0, type = TransactionType.EXPENSE, category = Category.TRANSPORT, description = "Taxi", createdAt = System.currentTimeMillis()),
    Transaction(id = 3, amount = 120000.0, type = TransactionType.INCOME, category = Category.SALARY, description = "Salary", createdAt = System.currentTimeMillis()),
)

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenPreview() {
    VoiceBudgetTheme {
        TransactionsContent(
            uiState = TransactionsUiState(isLoading = false, transactions = sampleTransactionsList),
            onCategorySelected = {},
            onDateRangeSelected = {},
            onStartEditing = {},
            onSaveEdit = {},
            onCancelEditing = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenEmptyPreview() {
    VoiceBudgetTheme {
        TransactionsContent(
            uiState = TransactionsUiState(isLoading = false),
            onCategorySelected = {},
            onDateRangeSelected = {},
            onStartEditing = {},
            onSaveEdit = {},
            onCancelEditing = {},
            onDelete = {},
        )
    }
}
