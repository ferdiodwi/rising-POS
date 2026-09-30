package com.rising.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Onboarding : Screen("onboarding", "Setup Awal")
    data object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    data object Products : Screen("products", "Produk", Icons.Default.Inventory2)
    data object Transactions : Screen("transactions", "Riwayat", Icons.Default.History)
    data object Dashboard : Screen("dashboard", "Laporan", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
}

val mainNavigationItems = listOf(
    Screen.Pos,
    Screen.Products,
    Screen.Transactions,
    Screen.Dashboard,
    Screen.Settings
)
