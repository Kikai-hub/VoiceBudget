package com.voicebudget.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.voicebudget.domain.model.CustomCategoryIcon

/** Icons offered when picking an icon for a user-defined category. */
fun customCategoryIcon(icon: CustomCategoryIcon): ImageVector = when (icon) {
    CustomCategoryIcon.TAG -> Icons.Filled.Sell
    CustomCategoryIcon.HOME -> Icons.Filled.Home
    CustomCategoryIcon.PETS -> Icons.Filled.Pets
    CustomCategoryIcon.SPORT -> Icons.Filled.SportsSoccer
    CustomCategoryIcon.FLIGHT -> Icons.Filled.Flight
    CustomCategoryIcon.SCHOOL -> Icons.Filled.School
    CustomCategoryIcon.CHILD_CARE -> Icons.Filled.ChildCare
    CustomCategoryIcon.SPA -> Icons.Filled.Spa
    CustomCategoryIcon.PHONE -> Icons.Filled.Phone
    CustomCategoryIcon.SHIELD -> Icons.Filled.Shield
    CustomCategoryIcon.BUILD -> Icons.Filled.Build
    CustomCategoryIcon.SUBSCRIPTIONS -> Icons.Filled.Subscriptions
    CustomCategoryIcon.PARK -> Icons.Filled.Park
    CustomCategoryIcon.VOLUNTEER -> Icons.Filled.VolunteerActivism
    CustomCategoryIcon.BOOK -> Icons.Filled.Book
    CustomCategoryIcon.MUSIC -> Icons.Filled.MusicNote
    CustomCategoryIcon.WORK -> Icons.Filled.Work
    CustomCategoryIcon.FITNESS -> Icons.Filled.FitnessCenter
    CustomCategoryIcon.PALETTE -> Icons.Filled.Palette
    CustomCategoryIcon.CELEBRATION -> Icons.Filled.Celebration
}

/** Palette offered when picking a color for a user-defined category, packed as opaque ARGB. */
val customCategoryColorPalette: List<Long> = listOf(
    0xFF00C896,
    0xFF00875A,
    0xFF34E0A1,
    0xFFFFC857,
    0xFFFF8A5C,
    0xFFFF5C72,
    0xFF6C5CE7,
    0xFF45556A,
    0xFF3B82F6,
    0xFFEC4899,
)
