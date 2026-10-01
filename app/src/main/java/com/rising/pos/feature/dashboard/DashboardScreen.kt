package com.rising.pos.feature.dashboard

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.components.WorkspaceHeader
import com.rising.pos.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {}
) {
    val metrics by viewModel.metrics.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    fun money(value: Long) = CurrencyFormatter.format(value, settings.currencySymbol)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { WorkspaceHeader("Laporan usaha", "Pantau penjualan dan kebutuhan toko.") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DashboardPeriod.entries) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(period.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
        item {
            ReportSurface {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Omzet · ${selectedPeriod.label}", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    Text(money(metrics.grossSales), style = MaterialTheme.typography.headlineLarge, color = Slate900)
                    Text("${metrics.transactionCount} transaksi selesai", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Slate200)
                    ReportValue("Rata-rata transaksi", money(metrics.averageTicketSize))
                    ReportValue("Pengeluaran", money(metrics.expenses))
                    ReportValue("Selisih pemasukan", money(metrics.netProfit))
                    Text("Selisih = omzet − pengeluaran tercatat. Belum memperhitungkan harga modal produk.",
                        style = MaterialTheme.typography.bodySmall, color = Slate500,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        // Stock needs attention independently of the selected sales period.
        if (metrics.lowStockProducts.isNotEmpty()) {
            item {
                Surface(
                    onClick = onNavigateToInventory,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${metrics.lowStockProducts.size} produk perlu restok", style = MaterialTheme.typography.titleSmall, color = Slate900)
                            Text("Periksa stok sebelum penjualan berikutnya.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Slate500)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Kelola usaha", style = MaterialTheme.typography.titleMedium, color = Slate900)
                ReportSurface {
                    Column {
                        OperationalRow("Stok produk", "${metrics.totalProductCount} produk aktif", Icons.Default.Inventory2, onNavigateToInventory)
                        HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                        OperationalRow("Pengeluaran", "Catat biaya operasional toko", Icons.Default.MoneyOff, onNavigateToExpenses)
                        HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                        OperationalRow("Pelanggan", "Kontak dan riwayat pembelian", Icons.Default.People, onNavigateToCustomers)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Produk terlaris", style = MaterialTheme.typography.titleMedium, color = Slate900)
                Text(selectedPeriod.label, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
        }
        if (metrics.topSellingProducts.isEmpty()) {
            item {
                ReportSurface {
                    WorkspaceEmptyState(Icons.Default.Receipt, "Belum ada penjualan", "Produk terlaris akan muncul setelah ada transaksi pada periode ini.")
                }
            }
        } else {
            item {
                ReportSurface {
                    Column {
                        metrics.topSellingProducts.forEachIndexed { index, product ->
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("${index + 1}".padStart(2, '0'), style = MaterialTheme.typography.labelLarge, color = Slate500)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(product.productName, style = MaterialTheme.typography.titleSmall, color = Slate900)
                                    Text("${product.totalQty.toInt()} item terjual", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    Text(money(product.totalRevenue), style = MaterialTheme.typography.labelLarge, color = Slate900)
                                }
                            }
                            if (index < metrics.topSellingProducts.lastIndex) HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Kondisi stok", style = MaterialTheme.typography.titleMedium, color = Slate900, modifier = Modifier.weight(1f))
                TextButton(onClick = onNavigateToInventory) { Text("Kelola stok") }
            }
        }
        if (metrics.lowStockProducts.isEmpty()) {
            item {
                Text("Tidak ada peringatan stok menipis.", style = MaterialTheme.typography.bodyMedium, color = Slate500)
            }
        } else {
            items(metrics.lowStockProducts, key = { it.id }) { product ->
                ReportSurface {
                    Column(Modifier.fillMaxWidth().clickable(onClick = onNavigateToInventory).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleSmall, color = Slate900)
                        Text(if (product.stock <= 0) "Stok habis" else "Sisa ${product.stock.toInt()} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (product.stock <= 0) DangerRed else WarningAmber)
                        Text("Minimum ${product.minStock.toInt()} ${product.unit}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSurface(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Slate200), content = content)
}

@Composable
private fun ReportValue(label: String, value: String) {
    // Stack the value when text scaling or large amounts would crowd a two-column row.
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Slate900)
    }
}

@Composable
private fun OperationalRow(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = Slate500, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Slate900)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Slate500)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Slate500, modifier = Modifier.size(20.dp))
    }
}
