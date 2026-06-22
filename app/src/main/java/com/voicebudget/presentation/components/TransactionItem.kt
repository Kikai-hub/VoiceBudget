package com.voicebudget.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.voicebudget.R
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(transaction.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${categoryLabel(transaction.category)} · ${formatDate(transaction.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
            val color = if (transaction.type == TransactionType.INCOME) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.error
            }
            Text(
                text = "$sign${formatAmount(transaction.amount, currencySymbol)}",
                color = color,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (onDelete != null) {
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
