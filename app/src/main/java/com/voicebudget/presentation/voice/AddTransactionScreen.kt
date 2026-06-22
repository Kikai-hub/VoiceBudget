package com.voicebudget.presentation.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.presentation.components.TransactionEditorDialog
import com.voicebudget.presentation.theme.Emerald500
import com.voicebudget.presentation.theme.Emerald700
import com.voicebudget.presentation.theme.EmeraldHeroGradient
import com.voicebudget.presentation.theme.VoiceBudgetTheme

@Composable
fun AddTransactionScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startListening()
    }

    val requestListening = {
        if (hasRecordAudioPermission(context)) {
            viewModel.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AddTransactionUiState.Saved) onDone()
    }

    AddTransactionContent(
        uiState = uiState,
        onMicClick = requestListening,
        onRetry = { viewModel.retry() },
        onCancel = onDone,
        onUpdateDraft = viewModel::updateDraft,
        onConfirm = viewModel::confirm,
        modifier = modifier,
    )
}

@Composable
private fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onMicClick: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onUpdateDraft: ((TransactionDraft) -> TransactionDraft) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is AddTransactionUiState.Idle -> IdleContent(onMicClick = onMicClick)
            is AddTransactionUiState.Listening -> ListeningContent()
            is AddTransactionUiState.Saving -> SavingContent()
            is AddTransactionUiState.Saved -> SavingContent()
            is AddTransactionUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = onRetry,
                onCancel = onCancel,
            )
            is AddTransactionUiState.Confirming -> {
                IdleContent(onMicClick = onMicClick)
                TransactionEditorDialog(
                    title = stringResource(R.string.confirm_transaction_title),
                    amountText = state.draft.amountText,
                    type = state.draft.type,
                    category = state.draft.category,
                    description = state.draft.description,
                    onAmountChange = { value -> onUpdateDraft { it.copy(amountText = value) } },
                    onTypeChange = { type ->
                        onUpdateDraft { it.copy(type = type, category = Category.other(type)) }
                    },
                    onCategoryChange = { category -> onUpdateDraft { it.copy(category = category) } },
                    onDescriptionChange = { value -> onUpdateDraft { it.copy(description = value) } },
                    onConfirm = onConfirm,
                    onDismiss = onRetry,
                )
            }
        }
    }
}

private fun hasRecordAudioPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

@Composable
private fun IdleContent(onMicClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Emerald500.copy(alpha = 0.14f)),
            )
            GradientMicButton(onClick = onMicClick)
        }
        Text(stringResource(R.string.voice_tap_to_speak), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ListeningContent() {
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 600), RepeatMode.Reverse),
        label = "scale",
    )
    val rippleScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200), RepeatMode.Restart),
        label = "ripple-scale",
    )
    val rippleAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200), RepeatMode.Restart),
        label = "ripple-alpha",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(scaleX = rippleScale, scaleY = rippleScale, alpha = rippleAlpha)
                    .clip(CircleShape)
                    .background(Emerald500),
            )
            GradientMicButton(
                onClick = {},
                contentDescription = stringResource(R.string.voice_listening_desc),
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
            )
        }
        Text(stringResource(R.string.voice_listening), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun GradientMicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.voice_start_recording),
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Emerald700, spotColor = Emerald700)
            .clip(CircleShape)
            .background(EmeraldHeroGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Mic,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun SavingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(stringResource(R.string.voice_saving), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message)
        Box(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = onRetry) { Text(stringResource(R.string.action_try_again)) }
        }
        TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
private fun AddTransactionScreenIdlePreview() {
    VoiceBudgetTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState.Idle,
            onMicClick = {}, onRetry = {}, onCancel = {}, onUpdateDraft = {}, onConfirm = {},
        )
    }
}

@Preview(showBackground = true, name = "Listening")
@Composable
private fun AddTransactionScreenListeningPreview() {
    VoiceBudgetTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState.Listening,
            onMicClick = {}, onRetry = {}, onCancel = {}, onUpdateDraft = {}, onConfirm = {},
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun AddTransactionScreenErrorPreview() {
    VoiceBudgetTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState.Error("Couldn't find an amount in \"Coffee\". Please try again."),
            onMicClick = {}, onRetry = {}, onCancel = {}, onUpdateDraft = {}, onConfirm = {},
        )
    }
}

@Preview(showBackground = true, name = "Confirming")
@Composable
private fun AddTransactionScreenConfirmingPreview() {
    VoiceBudgetTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState.Confirming(
                TransactionDraft(
                    amountText = "850",
                    type = TransactionType.EXPENSE,
                    category = Category.TRANSPORT,
                    description = "Taxi",
                ),
            ),
            onMicClick = {}, onRetry = {}, onCancel = {}, onUpdateDraft = {}, onConfirm = {},
        )
    }
}
