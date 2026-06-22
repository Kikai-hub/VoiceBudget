package com.voicebudget.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voicebudget.R
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.presentation.statistics.colorForCategory
import com.voicebudget.presentation.theme.IncomeGreen
import com.voicebudget.utils.formatAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    currencySymbol: String = "₽",
    onClick: (Transaction) -> Unit = {},
    onDelete: ((Transaction) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val categoryColor = colorForCategory(transaction.category)

    Card(
        onClick = { onClick(transaction) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(categoryIcon(transaction.category), contentDescription = null, tint = categoryColor)
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .weight(1f, fill = false),
                ) {
                    Text(
                        transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${categoryLabel(transaction.category)} · ${formatDate(transaction.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
                val color = if (transaction.type == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.error
                Text(
                    text = "$sign${formatAmount(transaction.amount, currencySymbol)}",
                    color = color,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    softWrap = false,
                )
                if (onDelete != null) {
                    IconButton(onClick = { onDelete(transaction) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
