package com.voicebudget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.presentation.AppRootViewModel
import com.voicebudget.presentation.navigation.VoiceBudgetNavHost
import com.voicebudget.presentation.onboarding.OnboardingFlow
import com.voicebudget.presentation.security.AppLockState
import com.voicebudget.presentation.security.LockScreen
import com.voicebudget.presentation.theme.AppThemeViewModel
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appLockState: AppLockState

    private val startVoiceEntryRequested = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val themeViewModel: AppThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val context = LocalContext.current
            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            VoiceBudgetTheme(darkTheme = useDarkTheme) {
                val appRootViewModel: AppRootViewModel = hiltViewModel()
                val hasWallet by appRootViewModel.hasWallet.collectAsState()
                val isLocked by appLockState.isLocked.collectAsState()
                when {
                    hasWallet == null || isLocked == null -> Box(modifier = Modifier.fillMaxSize())
                    hasWallet == false -> OnboardingFlow()
                    isLocked == true -> LockScreen()
                    else -> VoiceBudgetNavHost(
                        startVoiceEntry = startVoiceEntryRequested.value,
                        onStartVoiceEntryConsumed = { startVoiceEntryRequested.value = false },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_START_VOICE_ENTRY, false)) {
            startVoiceEntryRequested.value = true
        }
    }

    companion object {
        const val EXTRA_START_VOICE_ENTRY = "start_voice_entry"
    }
}
