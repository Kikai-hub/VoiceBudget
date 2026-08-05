package com.voicebudget.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.Transaction
import com.voicebudget.presentation.statistics.colorForCategory

/**
 * Resolves how a transaction's category should be displayed: its user-defined [CustomCategory]
 * when one is attached, falling back to the built-in [com.voicebudget.domain.model.Category]
 * otherwise.
 */
fun Transaction.resolveCustomCategory(customCategories: List<CustomCategory>): CustomCategory? =
    customCategoryId?.let { id -> customCategories.find { it.id == id } }

fun transactionCategoryIcon(transaction: Transaction, customCategories: List<CustomCategory>): ImageVector =
    transaction.resolveCustomCategory(customCategories)?.let { customCategoryIcon(it.icon) }
        ?: categoryIcon(transaction.category)

@Composable
fun transactionCategoryLabel(transaction: Transaction, customCategories: List<CustomCategory>): String =
    transaction.resolveCustomCategory(customCategories)?.name ?: categoryLabel(transaction.category)

fun transactionCategoryColor(transaction: Transaction, customCategories: List<CustomCategory>): Color =
    transaction.resolveCustomCategory(customCategories)?.let { Color(it.color.toInt()) }
        ?: colorForCategory(transaction.category)
