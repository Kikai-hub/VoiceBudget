package com.voicebudget.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.presentation.components.CurrencyPickerList
import com.voicebudget.presentation.components.LanguagePickerList
import com.voicebudget.utils.currencyLabel

@Composable
fun OnboardingFlow(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.step) {
        OnboardingStep.LANGUAGE -> OnboardingStepScaffold(
            title = stringResource(R.string.onboarding_language_step_title),
            nextEnabled = true,
            onNext = viewModel::goToCurrencyStep,
            modifier = modifier,
        ) {
            LanguagePickerList(
                selectedTag = uiState.selectedLanguageTag,
                onSelected = viewModel::selectLanguage,
                modifier = Modifier.fillMaxSize(),
            )
        }

        OnboardingStep.CURRENCY -> OnboardingStepScaffold(
            title = stringResource(R.string.onboarding_currency_step_title),
            nextEnabled = true,
            onNext = viewModel::goToWalletStep,
            modifier = modifier,
        ) {
            CurrencyPickerList(
                selected = uiState.selectedCurrency,
                onSelected = viewModel::selectCurrency,
                modifier = Modifier.fillMaxSize(),
            )
        }

        OnboardingStep.WALLET -> OnboardingStepScaffold(
            title = stringResource(R.string.onboarding_wallet_step_title),
            nextEnabled = uiState.walletName.isNotBlank() && !uiState.isCreating,
            nextLabel = stringResource(R.string.action_create_wallet),
            onNext = viewModel::createWallet,
            isLoading = uiState.isCreating,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.walletName,
                    onValueChange = viewModel::setWalletName,
                    label = { Text(stringResource(R.string.onboarding_wallet_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.wallets_field_currency) + ": " + currencyLabel(uiState.selectedCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepScaffold(
    title: String,
    nextEnabled: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    nextLabel: String? = null,
    isLoading: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(24.dp),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onNext, enabled = nextEnabled, modifier = Modifier.fillMaxWidth()) {
                    Text(nextLabel ?: stringResource(R.string.action_next))
                }
            }
        }
    }
}
