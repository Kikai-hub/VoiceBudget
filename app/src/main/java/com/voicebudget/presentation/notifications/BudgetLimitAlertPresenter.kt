package com.voicebudget.presentation.notifications

import android.content.Context
import com.voicebudget.domain.repository.CustomCategoryRepository
import com.voicebudget.domain.repository.WalletRepository
import com.voicebudget.domain.usecase.BudgetLimitAlert
import com.voicebudget.presentation.components.categoryLabel
import com.voicebudget.utils.formatAmount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class BudgetLimitAlertText(val categoryLabel: String, val spentText: String, val limitText: String)

/** Resolves a [BudgetLimitAlert] into display-ready strings for a notification. */
class BudgetLimitAlertPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customCategoryRepository: CustomCategoryRepository,
    private val walletRepository: WalletRepository,
) {
    suspend fun present(alert: BudgetLimitAlert): BudgetLimitAlertText {
        val limit = alert.limit
        val categoryLabel = limit.customCategoryId?.let { id ->
            customCategoryRepository.observeAll().first().firstOrNull { it.id == id }?.name
        } ?: limit.category?.let { categoryLabel(context, it) } ?: ""

        val currencySymbol = walletRepository.getWalletById(limit.walletId)?.currency?.symbol ?: ""
        return BudgetLimitAlertText(
            categoryLabel = categoryLabel,
            spentText = formatAmount(alert.spent, currencySymbol),
            limitText = formatAmount(limit.monthlyLimit, currencySymbol),
        )
    }
}
