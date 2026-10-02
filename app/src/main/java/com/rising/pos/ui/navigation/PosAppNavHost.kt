package com.rising.pos.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.feature.customer.CustomerScreen
import com.rising.pos.feature.dashboard.DashboardScreen
import com.rising.pos.feature.expense.ExpenseScreen
import com.rising.pos.feature.inventory.InventoryScreen
import com.rising.pos.feature.onboarding.OnboardingScreen
import com.rising.pos.feature.pos.PosScreen
import com.rising.pos.feature.product.ProductScreen
import com.rising.pos.feature.settings.SettingsScreen
import com.rising.pos.feature.transaction.TransactionScreen
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900

@Composable
fun PosAppNavHost(
    appPreferences: AppPreferences,
    settings: BusinessSettings? = null
) {
    val currentSettings = settings ?: run {
        val collected by appPreferences.settingsFlow.collectAsState(initial = null)
        collected
    }

    // Selama DataStore membaca preferensi dari disk saat startup,
    // tampilkan background kosong agar tidak flicker memunculkan layar Onboarding.
    if (currentSettings == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {}
        return
    }

    var localOnboardingDone by remember { mutableStateOf(false) }

    // Onboarding hanya muncul jika belum pernah diselesaikan, profil usaha belum ada,
    // dan belum diselesaikan pada sesi ini.
    val isOnboardingNeeded = !currentSettings.isOnboardingCompleted &&
            !localOnboardingDone &&
            !currentSettings.isActuallyOnboarded

    if (isOnboardingNeeded) {
        OnboardingScreen(
            onFinish = {
                localOnboardingDone = true
            }
        )
        return
    }

    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Pos.route

    if (isTablet) {
        // Tablet: NavigationRail layout with full items
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight()
            ) {
                tabletNavigationItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            screen.icon?.let { Icon(it, contentDescription = screen.title) }
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainAppNavHostContent(navController = navController)
            }
        }
    } else {
        // Phone: Scaffold with PlayStoreBottomNavBar
        val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000) ||
                (currentSettings.appTheme == com.rising.pos.core.model.AppTheme.DARK) ||
                (currentSettings.appTheme == com.rising.pos.core.model.AppTheme.SYSTEM && isSystemInDarkTheme())

        Scaffold(
            bottomBar = {
                PlayStoreBottomNavBar(
                    items = phoneNavigationItems,
                    currentRoute = currentRoute,
                    isDarkTheme = isDarkTheme,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                MainAppNavHostContent(navController = navController)
            }
        }
    }
}

@Composable
private fun MainAppNavHostContent(
    navController: androidx.navigation.NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Pos.route
    ) {
        composable(Screen.Pos.route) { PosScreen() }
        composable(Screen.Products.route) {
            ProductScreen(
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) }
            )
        }
        composable(Screen.Inventory.route) { InventoryScreen() }
        composable(Screen.Transactions.route) { TransactionScreen() }
        composable(Screen.Expenses.route) { ExpenseScreen() }
        composable(Screen.Customers.route) { CustomerScreen() }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Pos.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

