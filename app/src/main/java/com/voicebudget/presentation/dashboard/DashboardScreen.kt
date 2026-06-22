package com.voicebudget.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.MonthlySummary
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.presentation.components.EmptyState
import com.voicebudget.presentation.components.LoadingState
import com.voicebudget.presentation.components.TransactionItem
import com.voicebudget.presentation.theme.EmeraldHeroGradient
import com.voicebudget.presentation.theme.HeroCardShape
import com.voicebudget.presentation.theme.PillShape
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import com.voicebudget.utils.formatAmount

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardContent(uiState = uiState, modifier = modifier)
}

@Composable
private fun DashboardContent(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    if (uiState.isLoading) {
        LoadingState(modifier = modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        SummaryCard(
            income = uiState.summary.totalIncome,
            expense = uiState.summary.totalExpense,
            balance = uiState.summary.balance,
            currencySymbol = uiState.currencySymbol,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        Text(
            text = stringResource(R.string.dashboard_recent_transactions),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (uiState.recentTransactions.isEmpty()) {
            EmptyState(message = stringResource(R.string.dashboard_empty))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.recentTransactions, key = { it.id }) { transaction ->
                    TransactionItem(transaction = transaction, currencySymbol = uiState.currencySymbol)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    income: Double,
    expense: Double,
    balance: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(HeroCardShape)
            .background(EmeraldHeroGradient)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.this_month),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                text = formatAmount(balance, currencySymbol),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.headlineLarge.fontSize),
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    icon = Icons.Filled.ArrowUpward,
                    label = stringResource(R.string.income),
                    value = formatAmount(income, currencySymbol),
                )
                StatPill(
                    icon = Icons.Filled.ArrowDownward,
                    label = stringResource(R.string.expenses),
                    value = formatAmount(expense, currencySymbol),
                )
            }
        }
    }
}

@Composable
private fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = Color.White)
        }
    }
}

private val sampleDashboardTransactions = listOf(
    Transaction(id = 1, amount = 350.0, type = TransactionType.EXPENSE, category = Category.CAFE, description = "Coffee", createdAt = System.currentTimeMillis()),
    Transaction(id = 2, amount = 850.0, type = TransactionType.EXPENSE, category = Category.TRANSPORT, description = "Taxi", createdAt = System.currentTimeMillis()),
    Transaction(id = 3, amount = 120000.0, type = TransactionType.INCOME, category = Category.SALARY, description = "Salary", createdAt = System.currentTimeMillis()),
)

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    VoiceBudgetTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                summary = MonthlySummary(totalIncome = 120000.0, totalExpense = 1200.0),
                recentTransactions = sampleDashboardTransactions,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenEmptyPreview() {
    VoiceBudgetTheme {
        DashboardContent(uiState = DashboardUiState(isLoading = false))
    }
}
