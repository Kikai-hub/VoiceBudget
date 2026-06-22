package com.voicebudget.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

val VoiceBudgetTypography = base.copy(
    displayLarge = base.displayLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1).sp,
    ),
    headlineLarge = base.headlineLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = base.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontWeight = FontWeight.Medium),
    labelLarge = base.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
    ),
)

/** Tabular, extra-bold style reserved for the hero balance figure on the dashboard. */
val AmountHeroStyle = TextStyle(
    fontWeight = FontWeight.ExtraBold,
    fontSize = 40.sp,
    letterSpacing = (-1).sp,
)
