package com.voicebudget

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.domain.model.ThemeMode
import com.voicebudget.presentation.navigation.VoiceBudgetNavHost
import com.voicebudget.presentation.theme.AppThemeViewModel
import com.voicebudget.presentation.theme.VoiceBudgetTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: AppThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            VoiceBudgetTheme(darkTheme = useDarkTheme) {
                VoiceBudgetNavHost()
            }
        }
    }
}
