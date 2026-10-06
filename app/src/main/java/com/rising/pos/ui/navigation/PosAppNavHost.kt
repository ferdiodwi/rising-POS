package com.rising.pos.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

import androidx.compose.material.icons.outlined.Settings

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Pos.route

    PosAppScaffold(currentRoute, onNavigate = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }) {
        MainAppNavHostContent(navController = navController)
    }
}

/** Shared application shell keeps bottom navigation in both orientations. */
@Composable
internal fun PosAppScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        bottomBar = {
            PosBottomNavBar(
                expanded = configuration.screenWidthDp >= 600 && configuration.screenHeightDp >= 500 && currentRoute == Screen.Pos.route,
                items = phoneNavigationItems,
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(Modifier.fillMaxSize().padding(paddingValues), color = MaterialTheme.colorScheme.background) {
            content()
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
                },
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBackClick = null)
        }
    }
}

