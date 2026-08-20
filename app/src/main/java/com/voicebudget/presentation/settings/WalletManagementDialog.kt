package com.voicebudget.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.Currency
import com.voicebudget.domain.model.Wallet
import com.voicebudget.presentation.components.CurrencyPickerList
import com.voicebudget.utils.currencyLabel

@Composable
fun WalletManagementHost(
    onDismiss: () -> Unit,
    viewModel: WalletManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    WalletListDialog(
        uiState = uiState,
        onSwitchTo = viewModel::switchTo,
        onAddClick = viewModel::openAddDialog,
        onTransferClick = viewModel::openTransferDialog,
        onDismiss = onDismiss,
    )

    when (val state = dialogState) {
        is WalletDialogState.Adding -> NewWalletDialog(
            name = state.name,
            currency = state.currency,
            onNameChange = { name -> viewModel.updateDraft { it.copy(name = name) } },
            onCurrencyChange = { currency -> viewModel.updateDraft { it.copy(currency = currency) } },
            onConfirm = viewModel::confirmAdd,
            onDismiss = viewModel::dismissDialog,
        )
        is WalletDialogState.Transferring -> TransferDialog(
            draft = state.draft,
            onFromAmountChange = viewModel::updateTransferFromAmount,
            onToAmountChange = viewModel::updateTransferToAmount,
            onSwapDirection = viewModel::swapTransferDirection,
            onConfirm = viewModel::confirmTransfer,
            onDismiss = viewModel::dismissDialog,
        )
        WalletDialogState.Closed -> Unit
    }
}

@Composable
private fun WalletListDialog(
    uiState: WalletManagementUiState,
    onSwitchTo: (Long) -> Unit,
    onAddClick: () -> Unit,
    onTransferClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallets_screen_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.wallets.forEach { wallet ->
                    WalletRow(
                        wallet = wallet,
                        isActive = wallet.id == uiState.activeWalletId,
                        onClick = { onSwitchTo(wallet.id) },
                    )
                }
                if (uiState.canAddWallet) {
                    OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.wallets_add_button))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.wallets_max_reached),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (uiState.wallets.size == 2) {
                    OutlinedButton(onClick = onTransferClick, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.wallets_transfer_button))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
private fun TransferDialog(
    draft: TransferDraft,
    onFromAmountChange: (String) -> Unit,
    onToAmountChange: (String) -> Unit,
    onSwapDirection: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val canConfirm = !draft.isSubmitting &&
        (draft.fromAmountText.toDoubleOrNull() ?: 0.0) > 0.0 &&
        (draft.toAmountText.toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transfer_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(draft.fromWallet.name, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = onSwapDirection) {
                        Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.transfer_swap_direction))
                    }
                    Text(draft.toWallet.name, style = MaterialTheme.typography.bodyLarge)
                }
                OutlinedTextField(
                    value = draft.fromAmountText,
                    onValueChange = onFromAmountChange,
                    label = { Text(stringResource(R.string.transfer_field_from_amount, currencyLabel(draft.fromWallet.currency))) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.toAmountText,
                    onValueChange = onToAmountChange,
                    label = { Text(stringResource(R.string.transfer_field_to_amount, currencyLabel(draft.toWallet.currency))) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (draft.rateUnavailable) {
                    Text(
                        text = stringResource(R.string.transfer_rate_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = canConfirm) { Text(stringResource(R.string.transfer_confirm_button)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun WalletRow(wallet: Wallet, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(wallet.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = currencyLabel(wallet.currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = stringResource(R.string.wallets_active_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewWalletDialog(
    name: String,
    currency: Currency,
    onNameChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallets_new_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.wallets_field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.wallets_field_currency), style = MaterialTheme.typography.labelLarge)
                CurrencyPickerList(
                    selected = currency,
                    onSelected = onCurrencyChange,
                    modifier = Modifier.heightIn(max = 240.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = name.isNotBlank()) { Text(stringResource(R.string.action_create_wallet)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
