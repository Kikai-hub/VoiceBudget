package com.voicebudget.presentation.theme

import androidx.compose.ui.graphics.Color

// Brand: vibrant emerald fintech palette.
val Emerald100 = Color(0xFFD3F8E8)
val Emerald300 = Color(0xFF5CE6B3)
val Emerald400 = Color(0xFF34E0A1)
val Emerald500 = Color(0xFF00C896)
val Emerald600 = Color(0xFF00A87E)
val Emerald700 = Color(0xFF00875A)
val Emerald900 = Color(0xFF014D40)

// Income / expense semantic colors (coral instead of flat red for a softer, modern feel).
val IncomeGreen = Emerald500
val IncomeGreenDark = Emerald400
val ExpenseCoral = Color(0xFFFF5C72)
val ExpenseCoralDark = Color(0xFFFF8095)

// Neutral surfaces with a faint emerald tint.
val SurfaceLight = Color(0xFFFAFDFB)
val SurfaceVariantLight = Color(0xFFE7F2EC)
val BackgroundLight = Color(0xFFF3F8F6)
val OutlineLight = Color(0xFFD2E3DA)
val OnSurfaceLight = Color(0xFF10201A)

val SurfaceDark = Color(0xFF141B18)
val SurfaceVariantDark = Color(0xFF1E2925)
val BackgroundDark = Color(0xFF0D1311)
val OutlineDark = Color(0xFF2B3935)
val OnSurfaceDark = Color(0xFFE3F1EB)

// Legacy aliases kept for call sites that still reference the original names.
val GreenPrimary = Emerald700
val GreenPrimaryDark = Emerald400
val ExpenseRed = ExpenseCoral
