package com.voicebudget.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voicebudget.R
import com.voicebudget.presentation.dashboard.DashboardScreen
import com.voicebudget.presentation.settings.SettingsScreen
import com.voicebudget.presentation.statistics.StatisticsScreen
import com.voicebudget.presentation.transactions.TransactionsScreen
import com.voicebudget.presentation.voice.AddTransactionScreen

private data class BottomNavItem(val route: Routes, @param:androidx.annotation.StringRes val labelRes: Int, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.Dashboard, R.string.nav_dashboard, Icons.Filled.Home),
    BottomNavItem(Routes.Transactions, R.string.nav_transactions, Icons.AutoMirrored.Filled.List),
    BottomNavItem(Routes.Statistics, R.string.nav_statistics, Icons.Filled.BarChart),
    BottomNavItem(Routes.Settings, R.string.nav_settings, Icons.Filled.Settings),
)

@Composable
fun VoiceBudgetNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route.route,
                        onClick = {
                            navController.navigate(item.route.route) {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                        label = { },
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.Dashboard.route) {
                FloatingActionButton(onClick = { navController.navigate(Routes.AddTransaction.route) }) {
                    Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.nav_record_transaction))
                }
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(Routes.Dashboard.route) { DashboardScreen() }
            composable(Routes.Transactions.route) { TransactionsScreen() }
            composable(Routes.Statistics.route) { StatisticsScreen() }
            composable(Routes.Settings.route) { SettingsScreen() }
            composable(Routes.AddTransaction.route) {
                AddTransactionScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
