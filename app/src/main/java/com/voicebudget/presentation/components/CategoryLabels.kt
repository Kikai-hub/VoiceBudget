package com.voicebudget.presentation.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.categoryLabelRes

@Composable
fun categoryLabel(category: Category): String = stringResource(categoryLabelRes(category))

fun categoryLabel(context: Context, category: Category): String =
    context.getString(categoryLabelRes(category))

/**
 * Transactions saved without a typed description have a blank [Transaction.description] (never
 * a baked-in category name — that would freeze in whatever language was active at save time).
 * Resolve the fallback live here so it re-translates on language switch.
 */
@Composable
fun transactionDisplayDescription(transaction: Transaction, customCategories: List<CustomCategory> = emptyList()): String =
    transaction.description.ifBlank { transactionCategoryLabel(transaction, customCategories) }
