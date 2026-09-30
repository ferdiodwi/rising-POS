package com.rising.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    val icon: ImageVector? = selectedIcon
) {
    data object Onboarding : Screen("onboarding", "Setup Awal")
    data object Pos : Screen(
        route = "pos",
        title = "Kasir",
        selectedIcon = Icons.Filled.PointOfSale,
        unselectedIcon = Icons.Outlined.PointOfSale
    )
    data object Products : Screen(
        route = "products",
        title = "Produk",
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    )
    data object Inventory : Screen(
        route = "inventory",
        title = "Stok",
        selectedIcon = Icons.Filled.Inventory,
        unselectedIcon = Icons.Outlined.Inventory
    )
    data object Transactions : Screen(
        route = "transactions",
        title = "Riwayat",
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong
    )
    data object Expenses : Screen(
        route = "expenses",
        title = "Pengeluaran",
        selectedIcon = Icons.Filled.MoneyOff,
        unselectedIcon = Icons.Outlined.MoneyOff
    )
    data object Customers : Screen(
        route = "customers",
        title = "Pelanggan",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Laporan",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
    data object Settings : Screen(
        route = "settings",
        title = "Pengaturan",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val tabletNavigationItems = listOf(
    Screen.Pos,
    Screen.Products,
    Screen.Inventory,
    Screen.Transactions,
    Screen.Expenses,
    Screen.Customers,
    Screen.Dashboard,
    Screen.Settings
)

val phoneNavigationItems = listOf(
    Screen.Pos,
    Screen.Products,
    Screen.Transactions,
    Screen.Dashboard,
    Screen.Settings
)

