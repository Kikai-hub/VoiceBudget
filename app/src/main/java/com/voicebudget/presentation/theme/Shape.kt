package com.voicebudget.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VoiceBudgetShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Larger radius reserved for hero/feature surfaces such as the dashboard summary card. */
val HeroCardShape = RoundedCornerShape(28.dp)

/** Fully rounded shape for avatars, the mic FAB and pill-style chips. */
val PillShape = RoundedCornerShape(50)
