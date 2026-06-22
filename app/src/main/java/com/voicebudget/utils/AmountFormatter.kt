package com.voicebudget.utils

import java.util.Locale

fun formatAmount(amount: Double, currencySymbol: String = "₽"): String {
    val pattern = if (amount == Math.floor(amount)) "%,.0f" else "%,.2f"
    return "${String.format(Locale.US, pattern, amount)} $currencySymbol"
}
