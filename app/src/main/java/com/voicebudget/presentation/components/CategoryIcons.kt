package com.voicebudget.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.voicebudget.domain.model.Category

/** Icon shown inside each transaction's category avatar. */
fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.FOOD -> Icons.Filled.Restaurant
    Category.CAFE -> Icons.Filled.LocalCafe
    Category.TRANSPORT -> Icons.Filled.DirectionsCar
    Category.SHOPPING -> Icons.Filled.ShoppingBag
    Category.HEALTH -> Icons.Filled.LocalHospital
    Category.UTILITIES -> Icons.Filled.Bolt
    Category.ENTERTAINMENT -> Icons.Filled.Movie
    Category.OTHER_EXPENSE -> Icons.Filled.MoreHoriz
    Category.SALARY -> Icons.Filled.AccountBalanceWallet
    Category.FREELANCE -> Icons.Filled.Laptop
    Category.BONUS -> Icons.Filled.Star
    Category.GIFT -> Icons.Filled.CardGiftcard
    Category.OTHER_INCOME -> Icons.Filled.MoreHoriz
}
