package com.voicebudget.presentation.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.voicebudget.R
import com.voicebudget.domain.model.Category

@StringRes
fun categoryLabelRes(category: Category): Int = when (category) {
    Category.FOOD -> R.string.category_food
    Category.CAFE -> R.string.category_cafe
    Category.TRANSPORT -> R.string.category_transport
    Category.SHOPPING -> R.string.category_shopping
    Category.HEALTH -> R.string.category_health
    Category.UTILITIES -> R.string.category_utilities
    Category.ENTERTAINMENT -> R.string.category_entertainment
    Category.OTHER_EXPENSE, Category.OTHER_INCOME -> R.string.category_other
    Category.SALARY -> R.string.category_salary
    Category.FREELANCE -> R.string.category_freelance
    Category.BONUS -> R.string.category_bonus
    Category.GIFT -> R.string.category_gift
}

@Composable
fun categoryLabel(category: Category): String = stringResource(categoryLabelRes(category))

fun categoryLabel(context: Context, category: Category): String =
    context.getString(categoryLabelRes(category))
