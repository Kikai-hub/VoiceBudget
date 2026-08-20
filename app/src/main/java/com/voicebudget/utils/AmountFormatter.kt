package com.voicebudget.utils

import com.voicebudget.domain.model.Currency
import java.util.Locale

fun formatAmount(amount: Double, currencySymbol: String = "₽"): String {
    val pattern = if (amount == Math.floor(amount)) "%,.0f" else "%,.2f"
    return "${String.format(Locale.US, pattern, amount)} $currencySymbol"
}

fun currencyLabel(currency: Currency): String = "${currency.name} (${currency.symbol})"
