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
import androidx.compose.material.icons.filled.Print
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
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.PrimaryBlueLight
import com.rising.pos.ui.theme.PrimaryBlueContainer
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

    val isPrinting by viewModel.isPrinting.collectAsState()
    val printMessage by viewModel.printMessage.collectAsState()

    LaunchedEffect(printMessage) {
        printMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearPrintMessage()
        }
    }

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
                    Text("Pilih", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("Batal", color = Slate500)
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
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .background(MaterialTheme.colorScheme.surface)
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
                        color = Slate900
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = settings.name.ifBlank { "Warung Bu Siti" },
                        fontSize = 14.sp,
                        color = Slate500
                    )
                }
                IconButton(
                    onClick = { viewModel.printReport() },
                    enabled = !isPrinting
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Cetak Rekap Laporan",
                            tint = Slate900,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(onClick = { triggerExport() }) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = "Unduh",
                        tint = Slate900,
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
                    color = Slate500
                )
                Spacer(Modifier.weight(1f))
                if (metrics.isSampleData) {
                    Text(
                        text = "Data contoh",
                        fontSize = 12.sp,
                        color = Slate400
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
                    color = Slate500
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = money(metrics.grossSales),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Transaksi selesai saja.",
                    fontSize = 12.sp,
                    color = Slate400
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
                            color = Slate500
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${metrics.transactionCount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Slate200)
                    )

                    Spacer(Modifier.width(20.dp))

                    Column(Modifier.weight(1.5f)) {
                        Text(
                            text = "Rata-rata transaksi",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = money(metrics.averageTicketSize),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Slate100, thickness = 1.dp)
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
                    color = Slate900
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = metrics.monthRangeText,
                    fontSize = 12.sp,
                    color = Slate500
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
            HorizontalDivider(color = Slate100, thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Metode Pembayaran Section
            Text(
                text = "Metode pembayaran",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            PaymentMethodList(
                paymentSales = metrics.paymentSales,
                currencySymbol = settings.currencySymbol
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Slate100, thickness = 1.dp)
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
                    color = Slate900
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Terjual",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            Spacer(Modifier.height(10.dp))

            TopProductsList(topProducts = metrics.topSellingProducts)

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Slate100, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            // Footnote
            Text(
                text = "Transaksi batal tidak termasuk dalam laporan.",
                fontSize = 12.sp,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Unduh Laporan & Cetak Rekap Bottom Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { triggerExport() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, PrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Unduh CSV",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }

                Button(
                    onClick = { viewModel.printReport() },
                    enabled = !isPrinting,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Mencetak...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Cetak Rekap",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
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
    val bgColor = if (isSelected) PrimaryBlueContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) Slate200 else Slate200
    val contentColor = if (isSelected) PrimaryBlue else Slate700

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
                    color = if (isHighlighted) PrimaryBlue else Slate500,
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
                            color = if (isHighlighted) PrimaryBlue else PrimaryBlueLight,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )

                Spacer(Modifier.height(8.dp))

                // Day of month below bar
                Text(
                    text = point.label,
                    fontSize = 12.sp,
                    color = Slate500,
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
        Triple(PaymentMethod.CASH, "Tunai", paymentSales[PaymentMethod.CASH] ?: 0L),
        Triple(PaymentMethod.QRIS, "QRIS", paymentSales[PaymentMethod.QRIS] ?: 0L),
        Triple(PaymentMethod.BANK_TRANSFER, "Transfer", paymentSales[PaymentMethod.BANK_TRANSFER] ?: 0L),
        Triple(PaymentMethod.DEBIT_CARD, "Debit", paymentSales[PaymentMethod.DEBIT_CARD] ?: 0L)
    ).filter { it.third > 0L || it.first == PaymentMethod.CASH || it.first == PaymentMethod.QRIS }

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
                    tint = Slate600,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = name,
                    fontSize = 15.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = CurrencyFormatter.format(amount, currencySymbol),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Slate400,
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
                    color = Slate400,
                    modifier = Modifier.width(24.dp)
                )

                Text(
                    text = product.productName,
                    fontSize = 15.sp,
                    color = Slate900,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${product.totalQty.toInt()} $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate900
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
