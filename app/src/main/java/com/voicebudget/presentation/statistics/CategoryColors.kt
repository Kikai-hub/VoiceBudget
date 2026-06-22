package com.voicebudget.presentation.statistics

import androidx.compose.ui.graphics.Color
import com.voicebudget.domain.model.Category

private val palette = listOf(
    Color(0xFF00C896),
    Color(0xFF00875A),
    Color(0xFF34E0A1),
    Color(0xFFFFC857),
    Color(0xFFFF8A5C),
    Color(0xFFFF5C72),
    Color(0xFF6C5CE7),
    Color(0xFF45556A),
)

fun colorForCategory(category: Category): Color = palette[category.ordinal % palette.size]
