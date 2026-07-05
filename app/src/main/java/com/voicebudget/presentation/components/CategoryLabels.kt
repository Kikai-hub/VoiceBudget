package com.voicebudget.presentation.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.categoryLabelRes

@Composable
fun categoryLabel(category: Category): String = stringResource(categoryLabelRes(category))

fun categoryLabel(context: Context, category: Category): String =
    context.getString(categoryLabelRes(category))
