package com.rising.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Onboarding : Screen("onboarding", "Setup Awal")
    data object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    data object Products : Screen("products", "Produk", Icons.Default.Inventory2)
    data object Inventory : Screen("inventory", "Stok", Icons.Default.Inventory)
    data object Transactions : Screen("transactions", "Riwayat", Icons.Default.History)
    data object Expenses : Screen("expenses", "Pengeluaran", Icons.Default.MoneyOff)
    data object Customers : Screen("customers", "Pelanggan", Icons.Default.People)
    data object Dashboard : Screen("dashboard", "Laporan", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
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

