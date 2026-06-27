package com.voicebudget.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voicebudget.R
import com.voicebudget.presentation.dashboard.DashboardScreen
import com.voicebudget.presentation.settings.SettingsScreen
import com.voicebudget.presentation.statistics.StatisticsScreen
import com.voicebudget.presentation.theme.Emerald700
import com.voicebudget.presentation.theme.EmeraldHeroGradient
import com.voicebudget.presentation.transactions.TransactionsScreen
import com.voicebudget.presentation.voice.AddTransactionScreen

private data class BottomNavItem(val route: Routes, @param:androidx.annotation.StringRes val labelRes: Int, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.Dashboard, R.string.nav_dashboard, Icons.Filled.Home),
    BottomNavItem(Routes.Transactions, R.string.nav_transactions, Icons.AutoMirrored.Filled.List),
    BottomNavItem(Routes.Statistics, R.string.nav_statistics, Icons.Filled.BarChart),
    BottomNavItem(Routes.Settings, R.string.nav_settings, Icons.Filled.Settings),
)

private val NavBarShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun VoiceBudgetNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.clip(NavBarShape),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.Dashboard.route) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GradientMicFab(onClick = { navController.navigate(Routes.AddTransaction.route) })
                    ManualEntryFab(onClick = { navController.navigate(Routes.ManualTransaction.route) })
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
            composable(Routes.ManualTransaction.route) {
                AddTransactionScreen(onDone = { navController.popBackStack() }, manualEntry = true)
            }
        }
    }
}

@Composable
private fun GradientMicFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(elevation = 10.dp, shape = CircleShape, ambientColor = Emerald700, spotColor = Emerald700)
            .clip(CircleShape)
            .background(EmeraldHeroGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Mic,
            contentDescription = stringResource(R.string.nav_record_transaction),
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun ManualEntryFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = stringResource(R.string.nav_manual_transaction),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}
