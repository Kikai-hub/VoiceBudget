package com.voicebudget.presentation.statistics

import androidx.compose.ui.graphics.Color
import com.voicebudget.domain.model.Category

private val palette = listOf(
    Color(0xFF1B5E20),
    Color(0xFF2E7D32),
    Color(0xFF558B2F),
    Color(0xFFF9A825),
    Color(0xFFEF6C00),
    Color(0xFFD84315),
    Color(0xFF6D4C41),
    Color(0xFF455A64),
)

fun colorForCategory(category: Category): Color = palette[category.ordinal % palette.size]
