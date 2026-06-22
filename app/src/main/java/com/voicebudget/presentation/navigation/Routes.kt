package com.voicebudget.presentation.navigation

sealed class Routes(val route: String) {
    data object Dashboard : Routes("dashboard")
    data object AddTransaction : Routes("add_transaction")
    data object Transactions : Routes("transactions")
    data object Statistics : Routes("statistics")
    data object Settings : Routes("settings")
}
