package com.voicebudget.presentation.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R

@Composable
fun LockScreen(viewModel: AppLockViewModel = hiltViewModel()) {
    val pin by viewModel.pin.collectAsState()
    val error by viewModel.error.collectAsState()
    val biometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val biometricAvailable = remember(biometricEnabled) {
        biometricEnabled &&
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun launchBiometricPrompt() {
        val fragmentActivity = activity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.onBiometricSuccess()
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.security_enter_pin_title))
            .setNegativeButtonText(context.getString(R.string.action_cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(biometricAvailable) {
        if (biometricAvailable) launchBiometricPrompt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(stringResource(R.string.security_enter_pin_title), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = pin,
            onValueChange = viewModel::onPinChange,
            singleLine = true,
            isError = error,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(top = 24.dp, bottom = 8.dp),
        )
        if (error) {
            Text(
                stringResource(R.string.security_wrong_pin),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (biometricAvailable) {
            OutlinedButton(onClick = ::launchBiometricPrompt, modifier = Modifier.padding(top = 24.dp)) {
                Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.security_use_biometric_button))
            }
        }
    }
}
