package com.voicebudget.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.presentation.components.CurrencyPickerList
import com.voicebudget.presentation.components.LanguagePickerList
import com.voicebudget.presentation.theme.PillShape
import com.voicebudget.utils.currencyLabel

@Composable
fun OnboardingFlow(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.step) {
        OnboardingStep.LANGUAGE -> OnboardingStepScaffold(
            stepIndex = 0,
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
            stepIndex = 1,
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
            stepIndex = 2,
            title = stringResource(R.string.onboarding_wallet_step_title),
            nextEnabled = uiState.walletName.isNotBlank() && !uiState.isCreating,
            nextLabel = stringResource(R.string.action_create_wallet),
            onNext = viewModel::createWallet,
            isLoading = uiState.isCreating,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.walletName,
                    onValueChange = viewModel::setWalletName,
                    label = { Text(stringResource(R.string.onboarding_wallet_name_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
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

private const val TOTAL_ONBOARDING_STEPS = 3

@Composable
private fun OnboardingStepScaffold(
    stepIndex: Int,
    title: String,
    nextEnabled: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    nextLabel: String? = null,
    isLoading: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        StepProgressDots(
            stepIndex = stepIndex,
            totalSteps = TOTAL_ONBOARDING_STEPS,
            modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            content()
        }
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = onNext,
                    enabled = nextEnabled,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text(nextLabel ?: stringResource(R.string.action_next), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun StepProgressDots(stepIndex: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            val active = index <= stepIndex
            Box(
                modifier = Modifier
                    .size(if (index == stepIndex) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
            )
        }
    }
}
