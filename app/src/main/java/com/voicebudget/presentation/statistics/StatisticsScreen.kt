package com.voicebudget.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.presentation.components.categoryLabel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.voicebudget.presentation.components.EmptyState
import com.voicebudget.presentation.components.LoadingState
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import com.voicebudget.utils.formatAmount
import androidx.compose.runtime.LaunchedEffect

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    StatisticsContent(uiState = uiState, modifier = modifier)
}

@Composable
private fun StatisticsContent(uiState: StatisticsUiState, modifier: Modifier = Modifier) {
    if (uiState.isLoading) {
        LoadingState(modifier = modifier)
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatisticsSection(title = stringResource(R.string.stats_expense_breakdown)) {
            if (uiState.categoryBreakdown.isEmpty()) {
                EmptyState(message = stringResource(R.string.stats_no_expenses), modifier = Modifier.height(200.dp))
            } else {
                ExpenseBreakdownChart(
                    breakdown = uiState.categoryBreakdown,
                    currencySymbol = uiState.currencySymbol,
                )
            }
        }

        StatisticsSection(title = stringResource(R.string.stats_income_vs_expenses)) {
            MonthlyTrendChart(trend = uiState.monthlyTrend)
        }
    }
}

@Composable
private fun StatisticsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ExpenseBreakdownChart(
    breakdown: List<com.voicebudget.domain.model.CategoryAmount>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
) {
    val slices = breakdown.map { item ->
        PieChart.Slice(fill = Fill(SolidColor(colorForCategory(item.category))))
    }
    val modelProducer = remember { PieChartModelProducer() }
    val pieChart = rememberPieChart(sliceProvider = PieChart.SliceProvider.series(slices))

    LaunchedEffect(breakdown) {
        modelProducer.runTransaction {
            pieSeries { series(breakdown.map { it.amount }) }
        }
    }

    Column(modifier = modifier) {
        PieChartHost(
            pieChart,
            modelProducer,
            Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
        Column(modifier = Modifier.padding(top = 12.dp)) {
            breakdown.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colorForCategory(item.category)),
                        )
                        Text(categoryLabel(item.category), modifier = Modifier.padding(start = 8.dp))
                    }
                    Text(formatAmount(item.amount, currencySymbol))
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendChart(
    trend: List<com.voicebudget.domain.model.MonthlyTrendPoint>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val incomeColor = MaterialTheme.colorScheme.secondary
    val expenseColor = MaterialTheme.colorScheme.error

    LaunchedEffect(trend) {
        modelProducer.runTransaction {
            columnModel {
                series(trend.map { it.income })
                series(trend.map { it.expense })
            }
        }
    }

    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            LineComponent(fill = Fill(SolidColor(incomeColor))),
            LineComponent(fill = Fill(SolidColor(expenseColor))),
        ),
    )
    val chart = rememberCartesianChart(
        columnLayer,
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
    )

    CartesianChartHost(
        chart,
        modelProducer,
        modifier
            .fillMaxWidth()
            .height(220.dp),
    )
}

private val sampleBreakdown = listOf(
    com.voicebudget.domain.model.CategoryAmount(com.voicebudget.domain.model.Category.CAFE, 500.0),
    com.voicebudget.domain.model.CategoryAmount(com.voicebudget.domain.model.Category.TRANSPORT, 850.0),
    com.voicebudget.domain.model.CategoryAmount(com.voicebudget.domain.model.Category.FOOD, 2500.0),
)

private val sampleTrend = (0 until 6).map { offset ->
    com.voicebudget.domain.model.MonthlyTrendPoint(
        yearMonth = java.time.YearMonth.now().minusMonths((5 - offset).toLong()),
        income = if (offset == 5) 120000.0 else 0.0,
        expense = 1000.0 + offset * 200,
    )
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenPreview() {
    VoiceBudgetTheme {
        StatisticsContent(
            uiState = StatisticsUiState(isLoading = false, categoryBreakdown = sampleBreakdown, monthlyTrend = sampleTrend),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenEmptyPreview() {
    VoiceBudgetTheme {
        StatisticsContent(uiState = StatisticsUiState(isLoading = false, monthlyTrend = sampleTrend))
    }
}
