package com.voicebudget.presentation.advisor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.advisor.AdviceIcon
import com.voicebudget.domain.advisor.AdvicePriority
import com.voicebudget.domain.advisor.AdviceType
import com.voicebudget.domain.advisor.FinancialAdvice
import com.voicebudget.presentation.components.EmptyState
import com.voicebudget.presentation.components.LoadingState
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import com.voicebudget.utils.formatAmount

@Composable
fun AdvisorScreen(
    modifier: Modifier = Modifier,
    viewModel: AdvisorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
    ) { innerPadding ->
        AdvisorContent(
            uiState = uiState,
            onDismiss = viewModel::dismiss,
            onUpdateSettings = viewModel::updateSettings,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun AdvisorContent(
    uiState: AdvisorUiState,
    onDismiss: (String) -> Unit,
    onUpdateSettings: (AdvisorSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        LoadingState(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.advisor_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        if (uiState.advice.isEmpty()) {
            item {
                EmptyState(
                    message = stringResource(R.string.advisor_empty),
                    modifier = Modifier.fillParentMaxHeight(0.6f),
                )
            }
        } else {
            items(uiState.advice, key = { it.id }) { advice ->
                AdviceCard(
                    advice = advice,
                    currencySymbol = uiState.currencySymbol,
                    onDismiss = { onDismiss(advice.id) },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            AdvisorSettingsSection(
                settings = uiState.settings,
                onUpdate = onUpdateSettings,
            )
        }
    }
}

@Composable
private fun AdviceCard(
    advice: FinancialAdvice,
    currencySymbol: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = adviceIcon(advice.icon),
                    contentDescription = null,
                    tint = priorityColor(advice.priority),
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = advice.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = priorityLabel(advice.priority),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor(advice.priority),
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.advisor_dismiss),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = advice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    advice.potentialSavings?.let { savings ->
                        if (savings > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.advisor_potential_savings,
                                    formatAmount(savings, currencySymbol),
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisorSettingsSection(
    settings: AdvisorSettings,
    onUpdate: (AdvisorSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var smallPurchase by remember(settings) { mutableStateOf(settings.smallPurchaseThreshold.toLong().toString()) }
    var topCategoryThreshold by remember(settings) { mutableStateOf(settings.topCategoryThresholdPercent.toInt().toString()) }
    var savingsRate by remember(settings) { mutableStateOf(settings.desiredSavingsRatePercent.toInt().toString()) }
    var periodMonths by remember(settings) { mutableStateOf(settings.analysisPeriodMonths.toString()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.advisor_settings_title),
                style = MaterialTheme.typography.titleMedium,
            )

            AdvisorNumberField(
                label = stringResource(R.string.advisor_settings_small_purchase),
                value = smallPurchase,
                onValueChange = { smallPurchase = it },
            )
            AdvisorNumberField(
                label = stringResource(R.string.advisor_settings_top_category),
                value = topCategoryThreshold,
                onValueChange = { topCategoryThreshold = it },
            )
            AdvisorNumberField(
                label = stringResource(R.string.advisor_settings_savings_rate),
                value = savingsRate,
                onValueChange = { savingsRate = it },
            )
            AdvisorNumberField(
                label = stringResource(R.string.advisor_settings_period_months),
                value = periodMonths,
                onValueChange = { periodMonths = it },
            )

            TextButton(
                onClick = {
                    val updated = AdvisorSettings(
                        smallPurchaseThreshold = smallPurchase.toDoubleOrNull() ?: settings.smallPurchaseThreshold,
                        topCategoryThresholdPercent = topCategoryThreshold.toDoubleOrNull() ?: settings.topCategoryThresholdPercent,
                        desiredSavingsRatePercent = savingsRate.toDoubleOrNull() ?: settings.desiredSavingsRatePercent,
                        analysisPeriodMonths = periodMonths.toIntOrNull() ?: settings.analysisPeriodMonths,
                    )
                    onUpdate(updated)
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun AdvisorNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun priorityColor(priority: AdvicePriority): Color = when (priority) {
    AdvicePriority.CRITICAL -> MaterialTheme.colorScheme.error
    AdvicePriority.HIGH -> Color(0xFFE65100)
    AdvicePriority.MEDIUM -> Color(0xFFF57F17)
    AdvicePriority.LOW -> MaterialTheme.colorScheme.primary
}

@Composable
private fun priorityLabel(priority: AdvicePriority): String = when (priority) {
    AdvicePriority.CRITICAL -> stringResource(R.string.priority_critical)
    AdvicePriority.HIGH -> stringResource(R.string.priority_high)
    AdvicePriority.MEDIUM -> stringResource(R.string.priority_medium)
    AdvicePriority.LOW -> stringResource(R.string.priority_low)
}

fun adviceIcon(icon: AdviceIcon): ImageVector = when (icon) {
    AdviceIcon.TRENDING_UP -> Icons.Filled.TrendingUp
    AdviceIcon.TRENDING_DOWN -> Icons.Filled.TrendingDown
    AdviceIcon.SHOPPING -> Icons.Filled.ShoppingCart
    AdviceIcon.SAVINGS -> Icons.Filled.Savings
    AdviceIcon.INCOME -> Icons.Filled.AccountBalanceWallet
    AdviceIcon.REPEAT -> Icons.Filled.Repeat
    AdviceIcon.SUMMARY -> Icons.Filled.Assessment
    AdviceIcon.POSITIVE -> Icons.Filled.CheckCircle
}

@Preview(showBackground = true)
@Composable
private fun AdvisorScreenPreview() {
    VoiceBudgetTheme {
        AdvisorContent(
            uiState = AdvisorUiState(
                isLoading = false,
                advice = listOf(
                    FinancialAdvice(
                        id = "1",
                        title = "High spending this month",
                        description = "You spent 27% more this month than last month.",
                        priority = AdvicePriority.HIGH,
                        icon = AdviceIcon.TRENDING_UP,
                        type = AdviceType.HIGH_SPENDING,
                        potentialSavings = 5000.0,
                        createdAt = System.currentTimeMillis(),
                        dismissed = false,
                    ),
                    FinancialAdvice(
                        id = "2",
                        title = "Great savings!",
                        description = "You saved 22% of your income this month.",
                        priority = AdvicePriority.LOW,
                        icon = AdviceIcon.POSITIVE,
                        type = AdviceType.POSITIVE_PROGRESS,
                        potentialSavings = null,
                        createdAt = System.currentTimeMillis(),
                        dismissed = false,
                    ),
                ),
            ),
            onDismiss = {},
            onUpdateSettings = {},
        )
    }
}
