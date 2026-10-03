package com.rising.pos.feature.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.rising.pos.core.model.PaymentMethod
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer
import com.rising.pos.ui.theme.WarningAmber
import com.rising.pos.ui.theme.WarningAmberContainer

/**
 * Laporan usaha.
 *
 * Hierarki: omzet periode terpilih adalah satu-satunya angka utama. Metrik lain
 * turun tingkat di bawahnya. Semua nominal memakai figur tabular agar sejajar.
 */
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    // Snapshot the selected period when the document picker opens.
    val exportContent = androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri != null) scope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val output = context.contentResolver.openOutputStream(uri) ?: error("Tidak dapat membuka file")
                    output.bufferedWriter(Charsets.UTF_8).use { it.write(exportContent.value) }
                }.isSuccess
            }
            snackbar.showSnackbar(if (saved) "Laporan berhasil disimpan" else "Laporan gagal disimpan. Coba lagi.")
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { insets ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(insets),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Judul
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Laporan", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    "Pantau penjualan dan kebutuhan toko.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
            }
        }

        // Filter periode
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DashboardPeriod.entries) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(period.label) },
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedPeriod == period) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Slate200
                            }
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = Slate500,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Omzet: hero metric
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Omzet · ${selectedPeriod.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        money(metrics.grossSales),
                        style = PosTextStyles.displayMoney,
                        color = Slate900
                    )
                    Text(
                        "${metrics.transactionCount} transaksi selesai",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        }

        // Metrik sekunder
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = "Rata-rata transaksi",
                        value = money(metrics.averageTicketSize),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Pengeluaran",
                        value = money(metrics.expenses),
                        modifier = Modifier.weight(1f)
                    )
                }

                NetCard(
                    value = money(metrics.netProfit),
                    isPositive = metrics.netProfit >= 0
                )

                Text(
                    "Selisih dihitung dari omzet dikurangi pengeluaran tercatat. Belum memperhitungkan harga modal produk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        item {
            ReportSurface {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (selectedPeriod == DashboardPeriod.ALL_TIME) "Penjualan bulanan" else "Penjualan harian", style = MaterialTheme.typography.titleMedium)
                    if (metrics.dailySales.isEmpty()) {
                        Text("Grafik muncul setelah ada transaksi selesai.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        SalesChart(metrics.dailySales, settings.currencySymbol)
                        Text("Menampilkan tanggal dengan penjualan selesai.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                }
            }
        }
        if (metrics.paymentSales.isNotEmpty()) {
            item {
                ReportSurface {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Metode pembayaran", style = MaterialTheme.typography.titleMedium)
                        Text("Pembayaran terbagi mengikuti metode utama yang tercatat.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        metrics.paymentSales.forEach { (method, amount) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(paymentLabel(method), style = MaterialTheme.typography.bodyMedium)
                                Text(money(amount), style = PosTextStyles.money)
                            }
                        }
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    exportContent.value = buildString {
                        appendLine("Ringkasan,Nilai")
                        appendLine("Periode,${selectedPeriod.label}")
                        appendLine("Penjualan,${metrics.grossSales}")
                        appendLine("Transaksi selesai,${metrics.transactionCount}")
                        appendLine("Rata-rata transaksi,${metrics.averageTicketSize}")
                        appendLine("Pengeluaran,${metrics.expenses}")
                        appendLine("Selisih,${metrics.netProfit}")
                        appendLine()
                        appendLine("Tanggal,Penjualan")
                        metrics.dailySales.forEach { appendLine("${it.label},${it.amount}") }
                        appendLine()
                        appendLine("Metode pembayaran,Penjualan")
                        metrics.paymentSales.forEach { (method, amount) -> appendLine("${paymentLabel(method)},$amount") }
                    }
                    exportLauncher.launch("Laporan-Rising-${java.time.LocalDate.now()}.csv")
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Unduh laporan") }
        }

        // Peringatan stok
        if (metrics.lowStockProducts.isNotEmpty()) {
            item {
                Surface(
                    onClick = onNavigateToInventory,
                    color = WarningAmberContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "${metrics.lowStockProducts.size} produk perlu restok",
                                style = MaterialTheme.typography.titleSmall,
                                color = Slate900
                            )
                            Text(
                                "Periksa stok sebelum penjualan berikutnya.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Slate500)
                    }
                }
            }
        }

        // Kelola usaha
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Kelola usaha", style = MaterialTheme.typography.titleMedium, color = Slate900)
                ReportSurface {
                    Column {
                        OperationalRow(
                            title = "Stok produk",
                            description = "${metrics.totalProductCount} produk aktif",
                            icon = Icons.Outlined.Inventory2,
                            onClick = onNavigateToInventory
                        )
                        HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                        OperationalRow(
                            title = "Pengeluaran",
                            description = "Catat biaya operasional toko",
                            icon = Icons.Outlined.MoneyOff,
                            onClick = onNavigateToExpenses
                        )
                        HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                        OperationalRow(
                            title = "Pelanggan",
                            description = "Kontak dan riwayat pembelian",
                            icon = Icons.Outlined.People,
                            onClick = onNavigateToCustomers
                        )
                    }
                }
            }
        }

        // Produk terlaris
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Produk terlaris", style = MaterialTheme.typography.titleMedium, color = Slate900)
                Text(selectedPeriod.label, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
        }
        if (metrics.topSellingProducts.isEmpty()) {
            item {
                ReportSurface {
                    WorkspaceEmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = "Belum ada penjualan",
                        description = "Produk terlaris akan muncul setelah ada transaksi pada periode ini."
                    )
                }
            }
        } else {
            item {
                ReportSurface {
                    Column {
                        metrics.topSellingProducts.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RankBadge(rank = index + 1, highlighted = index < 3)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(product.productName, style = MaterialTheme.typography.titleSmall, color = Slate900)
                                    Text(
                                        "${product.totalQty.toInt()} item terjual",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    money(product.totalRevenue),
                                    style = PosTextStyles.money,
                                    color = Slate900
                                )
                            }
                            if (index < metrics.topSellingProducts.lastIndex) {
                                HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Kondisi stok
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Kondisi stok", style = MaterialTheme.typography.titleMedium, color = Slate900, modifier = Modifier.weight(1f))
                TextButton(onClick = onNavigateToInventory) {
                    Text("Kelola stok", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (metrics.lowStockProducts.isEmpty()) {
            item {
                ReportSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Tidak ada peringatan stok menipis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate700
                        )
                    }
                }
            }
        } else {
            items(metrics.lowStockProducts, key = { it.id }) { product ->
                val habis = product.stock <= 0
                ReportSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (habis) Icons.Outlined.Block else Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = if (habis) DangerRed else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall, color = Slate900)
                            Text(
                                if (habis) "Stok habis" else "Sisa ${product.stock.toInt()} ${product.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (habis) DangerRed else WarningAmber
                            )
                            Text(
                                "Minimum ${product.minStock.toInt()} ${product.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Slate500, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun ReportSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200),
        content = content
    )
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Slate500)
            Text(value, style = PosTextStyles.priceCard, color = Slate900)
        }
    }
}

/**
 * Kartu selisih pemasukan. Positif/negatif ditandai ikon + teks, bukan warna saja.
 */
@Composable
private fun NetCard(value: String, isPositive: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isPositive) SuccessGreenContainer else DangerRedContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                contentDescription = null,
                tint = if (isPositive) SuccessGreen else DangerRed,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Selisih pemasukan", style = MaterialTheme.typography.labelMedium, color = Slate700)
                Text(
                    if (isPositive) "Surplus periode ini" else "Defisit periode ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
            }
            Text(
                value,
                style = PosTextStyles.money,
                color = if (isPositive) SuccessGreen else DangerRed
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int, highlighted: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.size(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                color = if (highlighted) MaterialTheme.colorScheme.primary else Slate500
            )
        }
    }
}

@Composable
private fun OperationalRow(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Slate900)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Slate500, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SalesChart(points: List<SalesPoint>, currencySymbol: String) {
    val maximum = points.maxOf { it.amount }.coerceAtLeast(1L)
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            Column(
                Modifier.widthIn(min = 64.dp).semantics {
                    contentDescription = "${point.label}: ${CurrencyFormatter.format(point.amount, currencySymbol)}"
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(CurrencyFormatter.format(point.amount, currencySymbol), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.height(100.dp).width(32.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.fillMaxWidth().fillMaxHeight((point.amount.toFloat() / maximum).coerceIn(0f, 1f)).background(primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                }
                Spacer(Modifier.height(8.dp))
                Text(point.label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            }
        }
    }
}

private fun paymentLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer bank"
    PaymentMethod.DEBIT_CARD -> "Kartu debit"
    PaymentMethod.CREDIT_CARD -> "Kartu kredit"
    PaymentMethod.E_WALLET -> "Dompet digital"
    PaymentMethod.OTHER -> "Lainnya"
}
