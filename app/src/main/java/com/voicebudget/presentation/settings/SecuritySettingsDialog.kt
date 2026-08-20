package com.voicebudget.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R

@Composable
fun SecuritySettingsHost(
    onDismiss: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val pinSetupState by viewModel.pinSetupState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_security)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.security_pin_toggle), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = settings.isPinSet,
                        onCheckedChange = { enabled -> if (enabled) viewModel.startSettingPin() else viewModel.disableProtection() },
                    )
                }
                if (settings.isPinSet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.security_biometric_toggle), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = settings.isBiometricEnabled, onCheckedChange = viewModel::setBiometricEnabled)
                    }
                    TextButton(onClick = viewModel::startSettingPin) {
                        Text(stringResource(R.string.security_change_pin_button))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )

    when (val state = pinSetupState) {
        is PinSetupState.EnteringNew -> PinEntryDialog(
            title = stringResource(R.string.security_set_pin_title),
            value = state.pin,
            error = false,
            onValueChange = viewModel::onNewPinChange,
            onDismiss = viewModel::cancelPinSetup,
        )
        is PinSetupState.Confirming -> PinEntryDialog(
            title = stringResource(R.string.security_confirm_pin_title),
            value = state.confirmPin,
            error = state.mismatch,
            errorText = stringResource(R.string.security_pin_mismatch),
            onValueChange = viewModel::onConfirmPinChange,
            onDismiss = viewModel::cancelPinSetup,
        )
        PinSetupState.Closed -> Unit
    }
}

@Composable
private fun PinEntryDialog(
    title: String,
    value: String,
    error: Boolean,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    errorText: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    isError = error,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error && errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
