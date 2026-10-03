package com.rising.pos.feature.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.CashierIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
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
    val exportContent = rememberSaveable { mutableStateOf("") }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        val output = context.contentResolver.openOutputStream(uri) ?: error("Gagal membuka file")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(exportContent.value) }
                    }.isSuccess
                }
                snackbar.showSnackbar(if (saved) "Laporan berhasil diunduh" else "Gagal mengunduh laporan. Coba lagi.")
            }
        }
    }

    fun triggerExport() {
        exportContent.value = buildString {
            appendLine("Laporan Penjualan - ${settings.name.ifBlank { "Warung Bu Siti" }}")
            appendLine("Periode,${metrics.dateRangeText}")
            appendLine("Total Penjualan,${metrics.grossSales}")
            appendLine("Jumlah Transaksi,${metrics.transactionCount}")
            appendLine("Rata-rata Transaksi,${metrics.averageTicketSize}")
            appendLine()
            appendLine("Penjualan Harian")
            appendLine("Hari,Penjualan")
            metrics.dailySales.forEach { appendLine("${it.label},${it.amount}") }
            appendLine()
            appendLine("Metode Pembayaran")
            appendLine("Metode,Penjualan")
            metrics.paymentSales.forEach { (method, amount) -> appendLine("${paymentLabel(method)},$amount") }
            appendLine()
            appendLine("Produk Terlaris")
            appendLine("Peringkat,Produk,Terjual")
            metrics.topSellingProducts.forEachIndexed { i, p -> appendLine("${i + 1},${p.productName},${p.totalQty.toInt()}") }
        }
        exportLauncher.launch("Laporan-Penjualan-${LocalDate.now()}.csv")
    }

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            viewModel.setCustomDateRange(start, end)
                        }
                        showDateRangePicker = false
                    }
                ) {
                    Text("Pilih", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        "Pilih Rentang Tanggal",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                headline = { },
                showModeToggle = false
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Top Bar
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Laporan",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = settings.name.ifBlank { "Warung Bu Siti" },
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
                IconButton(onClick = { triggerExport() }) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = "Unduh",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Filter Tabs (Hari ini | 7 hari | Pilih tanggal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hari ini
                PeriodButton(
                    text = "Hari ini",
                    isSelected = selectedPeriod == DashboardPeriod.TODAY,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setPeriod(DashboardPeriod.TODAY) }
                )

                // 7 hari
                PeriodButton(
                    text = "7 hari",
                    isSelected = selectedPeriod == DashboardPeriod.LAST_7_DAYS,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setPeriod(DashboardPeriod.LAST_7_DAYS) }
                )

                // Pilih tanggal
                PeriodButton(
                    text = "Pilih tanggal",
                    isSelected = selectedPeriod == DashboardPeriod.CUSTOM,
                    icon = Icons.Outlined.CalendarToday,
                    modifier = Modifier.weight(1.3f),
                    onClick = { showDateRangePicker = true }
                )
            }

            Spacer(Modifier.height(14.dp))

            // Subtitle row: Date Range on Left, "Data contoh" on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metrics.dateRangeText,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.weight(1f))
                if (metrics.isSampleData) {
                    Text(
                        text = "Data contoh",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Hero Total Penjualan Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Total penjualan",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = money(metrics.grossSales),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Transaksi selesai saja.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(Modifier.height(18.dp))

                // 2 Metrik Row (Transaksi & Rata-rata transaksi)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Transaksi",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${metrics.transactionCount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Spacer(Modifier.width(20.dp))

                    Column(Modifier.weight(1.5f)) {
                        Text(
                            text = "Rata-rata transaksi",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = money(metrics.averageTicketSize),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Penjualan Harian Section (Bar Chart)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Penjualan harian",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = metrics.monthRangeText,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Daily Sales Bar Chart
            if (metrics.dailySales.isNotEmpty()) {
                DailySalesBarChart(
                    points = metrics.dailySales,
                    currencySymbol = settings.currencySymbol
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Metode Pembayaran Section
            Text(
                text = "Metode pembayaran",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            PaymentMethodList(
                paymentSales = metrics.paymentSales,
                currencySymbol = settings.currencySymbol
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Produk Terlaris Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produk terlaris",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Terjual",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(10.dp))

            TopProductsList(topProducts = metrics.topSellingProducts)

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            // Footnote
            Text(
                text = "Transaksi batal tidak termasuk dalam laporan.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Unduh Laporan Bottom Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { triggerExport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2563EB)
                    )
                ) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Unduh laporan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PeriodButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
    val borderColor = if (isSelected) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)
    val contentColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF334155)

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DailySalesBarChart(
    points: List<SalesPoint>,
    currencySymbol: String
) {
    val maxAmount = points.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
    val maxHeightDp = 100f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val ratio = (point.amount.toFloat() / maxAmount).coerceIn(0f, 1f)
            val barHeight = (ratio * maxHeightDp).coerceAtLeast(4f).dp
            val isHighlighted = point.isHighlighted

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Nominal above bar
                Text(
                    text = CurrencyFormatter.format(point.amount, currencySymbol),
                    fontSize = 9.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHighlighted) Color(0xFF2563EB) else Color(0xFF64748B),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                // The Bar itself
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(barHeight)
                        .background(
                            color = if (isHighlighted) Color(0xFF2563EB) else Color(0xFF60A5FA),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )

                Spacer(Modifier.height(8.dp))

                // Day of month below bar
                Text(
                    text = point.label,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodList(
    paymentSales: Map<PaymentMethod, Long>,
    currencySymbol: String
) {
    val items = listOf(
        Triple(PaymentMethod.CASH, "Tunai", paymentSales[PaymentMethod.CASH] ?: 830_000L),
        Triple(PaymentMethod.QRIS, "QRIS", paymentSales[PaymentMethod.QRIS] ?: 290_000L),
        Triple(PaymentMethod.BANK_TRANSFER, "Transfer", paymentSales[PaymentMethod.BANK_TRANSFER] ?: 125_000L)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        items.forEach { (method, name, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (method) {
                    PaymentMethod.CASH -> CashierIcons.CashRegister
                    PaymentMethod.QRIS -> Icons.Outlined.QrCodeScanner
                    PaymentMethod.BANK_TRANSFER -> CashierIcons.Bank
                    else -> CashierIcons.Bank
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = name,
                    fontSize = 15.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = CurrencyFormatter.format(amount, currencySymbol),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TopProductsList(
    topProducts: List<com.rising.pos.core.database.entity.TopSellingProduct>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        topProducts.take(5).forEachIndexed { index, product ->
            val unit = when {
                product.productName.contains("Teh Botol", ignoreCase = true) -> "botol"
                product.productName.contains("Minyak", ignoreCase = true) -> "pcs"
                else -> "pcs"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.width(24.dp)
                )

                Text(
                    text = product.productName,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${product.totalQty.toInt()} $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

private fun paymentLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer"
    PaymentMethod.DEBIT_CARD -> "Kartu Debit"
    PaymentMethod.CREDIT_CARD -> "Kartu Kredit"
    PaymentMethod.E_WALLET -> "Dompet Digital"
    PaymentMethod.OTHER -> "Lainnya"
}
