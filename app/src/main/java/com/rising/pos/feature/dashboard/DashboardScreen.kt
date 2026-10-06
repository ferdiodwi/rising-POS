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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ShowChart
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
import androidx.compose.ui.platform.LocalConfiguration
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
import java.util.Locale
import com.rising.pos.core.datastore.BusinessSettings

import coil.compose.AsyncImage
import androidx.compose.material.icons.outlined.Inventory2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val isWideScreen = screenWidth >= 600
    val isCompactHeight = screenHeight < 500

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
            appendLine("Laporan Penjualan - ${settings.name.ifBlank { "Rising Studio" }}")
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
        if (isWideScreen && !isCompactHeight) {
            DashboardTabletLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets),
                metrics = metrics,
                settings = settings,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { viewModel.setPeriod(it) },
                onSelectDateRangeClick = { showDateRangePicker = true },
                onExportClick = { triggerExport() },
                onPrintClick = { viewModel.printReport() },
                isPrinting = isPrinting,
                onNavigateToInventory = onNavigateToInventory
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.rising.pos.ui.components.PosHeaderTitleSection(
                        title = "Laporan",
                        subtitle = settings.name.ifBlank { "Rising Studio" },
                        modifier = Modifier.weight(1f)
                    )
                    com.rising.pos.ui.components.PosHeaderActionButton(
                        icon = Icons.Default.Print,
                        contentDescription = "Cetak Rekap Laporan",
                        onClick = { viewModel.printReport() },
                        enabled = !isPrinting,
                        isLoading = isPrinting
                    )
                    Spacer(Modifier.width(8.dp))
                    com.rising.pos.ui.components.PosHeaderActionButton(
                        icon = Icons.Outlined.FileDownload,
                        contentDescription = "Unduh",
                        onClick = { triggerExport() }
                    )
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
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = "Hari ini",
                        selected = selectedPeriod == DashboardPeriod.TODAY,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setPeriod(DashboardPeriod.TODAY) }
                    )

                    // 7 hari
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = "7 hari",
                        selected = selectedPeriod == DashboardPeriod.LAST_7_DAYS,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setPeriod(DashboardPeriod.LAST_7_DAYS) }
                    )

                    // Pilih tanggal
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = "Pilih tanggal",
                        selected = selectedPeriod == DashboardPeriod.CUSTOM,
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

                // ── Tampilan 1-Kolom Standar (HP Portrait) ────────────────────
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

                if (metrics.dailySales.isNotEmpty()) {
                    DailySalesBarChart(
                        points = metrics.dailySales,
                        currencySymbol = settings.currencySymbol
                    )
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Slate100, thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

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

                Text(
                    text = "Transaksi batal tidak termasuk dalam laporan.",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(16.dp))

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

@Composable
private fun DashboardTabletLayout(
    modifier: Modifier = Modifier,
    metrics: DashboardMetrics,
    settings: BusinessSettings,
    selectedPeriod: DashboardPeriod,
    onPeriodSelected: (DashboardPeriod) -> Unit,
    onSelectDateRangeClick: () -> Unit,
    onExportClick: () -> Unit,
    onPrintClick: () -> Unit,
    isPrinting: Boolean,
    onNavigateToInventory: () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 1. Header Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.rising.pos.ui.components.PosHeaderTitleSection(
                title = "Laporan",
                subtitle = settings.name.ifBlank { "Rising Studio" },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onExportClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ekspor laporan",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── 2. Filter Bar (Tabs + Range Text) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                DashboardPeriod.TODAY to "Hari ini",
                DashboardPeriod.LAST_7_DAYS to "7 hari",
                DashboardPeriod.THIS_MONTH to "Bulan ini"
            ).forEach { (p, label) ->
                val isSelected = selectedPeriod == p
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryBlueContainer else Color.White)
                        .border(1.dp, if (isSelected) PrimaryBlue else Slate200, RoundedCornerShape(8.dp))
                        .clickable { onPeriodSelected(p) }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) PrimaryBlue else Slate700
                    )
                }
            }

            val isCustom = selectedPeriod == DashboardPeriod.CUSTOM
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCustom) PrimaryBlueContainer else Color.White)
                    .border(1.dp, if (isCustom) PrimaryBlue else Slate200, RoundedCornerShape(8.dp))
                    .clickable { onSelectDateRangeClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = if (isCustom) PrimaryBlue else Slate500,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Pilih tanggal",
                        fontSize = 13.sp,
                        fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isCustom) PrimaryBlue else Slate700
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = metrics.dateRangeText,
                fontSize = 13.sp,
                color = Slate600
            )

            if (metrics.isSampleData) {
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Data contoh",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── 3. Tiga Stat Cards Horizontal ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Total Penjualan
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ShowChart,
                iconBgColor = Color(0xFFDCFCE7),
                iconTint = Color(0xFF16A34A),
                title = "Total penjualan",
                value = CurrencyFormatter.format(metrics.grossSales, settings.currencySymbol),
                subtext = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = metrics.trendPercentage,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )

            // Card 2: Transaksi Selesai
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ShoppingCart,
                iconBgColor = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                title = "Transaksi selesai",
                value = "${metrics.transactionCount}",
                subtext = {
                    Text(
                        text = if (selectedPeriod == DashboardPeriod.LAST_7_DAYS) "Dalam 7 hari terakhir" else "Transaksi selesai",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            )

            // Card 3: Rata-rata Transaksi
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                iconBgColor = Color(0xFFF3E8FF),
                iconTint = Color(0xFF9333EA),
                title = "Rata-rata transaksi",
                value = CurrencyFormatter.format(metrics.averageTicketSize, settings.currencySymbol),
                subtext = {
                    Text(
                        text = "Per transaksi selesai",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── 4. Dua Kolom Konten (Tren Penjualan & Produk Terlaris) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Kolom Kiri: Tren Penjualan
            Column(modifier = Modifier.weight(1.35f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Tren penjualan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Penjualan harian",
                            fontSize = 12.5.sp,
                            color = Slate500
                        )

                        Spacer(Modifier.height(20.dp))

                        SalesTrendBarChart(
                            dailyPoints = metrics.dailySales,
                            currencySymbol = settings.currencySymbol
                        )

                        Spacer(Modifier.height(18.dp))
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rata-rata per hari",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = CurrencyFormatter.format(metrics.dailyAverageSales, settings.currencySymbol),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = metrics.highestDayText,
                                fontSize = 13.sp,
                                color = Slate600
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Hanya transaksi selesai yang dihitung dalam laporan.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            // Kolom Kanan: Produk Terlaris
            Column(modifier = Modifier.weight(0.95f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Produk terlaris",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Berdasarkan jumlah terjual",
                            fontSize = 12.5.sp,
                            color = Slate500
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Produk", fontSize = 12.sp, color = Slate500)
                            Spacer(Modifier.weight(1f))
                            Text("Terjual", fontSize = 12.sp, color = Slate500)
                        }

                        Spacer(Modifier.height(8.dp))

                        val displayProducts = if (metrics.topProductsDisplay.isNotEmpty()) {
                            metrics.topProductsDisplay
                        } else {
                            metrics.topSellingProducts.mapIndexed { idx, p ->
                                TopProductDisplayItem(
                                    rank = idx + 1,
                                    productId = p.productId,
                                    productName = p.productName,
                                    categoryName = "Produk",
                                    totalQtyText = "${p.totalQty.toInt()} pcs"
                                )
                            }
                        }

                        displayProducts.take(5).forEach { item ->
                            TopProductTabletRow(item = item)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .clickable(onClick = onNavigateToInventory)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lihat semua produk >",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    value: String,
    subtext: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBgColor, RoundedCornerShape(21.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    color = Slate500
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(Modifier.height(4.dp))
                subtext()
            }
        }
    }
}

@Composable
private fun SalesTrendBarChart(
    dailyPoints: List<SalesPoint>,
    currencySymbol: String
) {
    val maxAmount = 2_000_000L.coerceAtLeast(dailyPoints.maxOfOrNull { it.amount } ?: 2_000_000L)
    val levels = listOf(
        maxAmount,
        (maxAmount * 0.75).toLong(),
        (maxAmount * 0.50).toLong(),
        (maxAmount * 0.25).toLong(),
        0L
    )

    fun formatShort(amount: Long): String = when {
        amount >= 1_000_000L -> {
            val jt = amount.toDouble() / 1_000_000.0
            if (jt % 1.0 == 0.0) "Rp${jt.toInt()} jt" else "Rp${String.format(Locale.US, "%.1f", jt).replace('.', ',')} jt"
        }
        amount >= 1_000L -> "Rp${amount / 1000} rb"
        else -> "Rp0"
    }

    val chartHeightDp = 140.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        // Y-Axis labels (left)
        Column(
            modifier = Modifier
                .width(58.dp)
                .height(chartHeightDp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            levels.forEach { lvl ->
                Text(
                    text = formatShort(lvl),
                    fontSize = 10.sp,
                    color = Slate500,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Chart area with horizontal grid lines and bars
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Horizontal grid lines
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeightDp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Slate100)
                    )
                }
            }

            // Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyPoints.forEach { point ->
                    val ratio = (point.amount.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                    val barHeight = (ratio * chartHeightDp.value).dp.coerceAtLeast(4.dp)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // The Bar
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(barHeight)
                                .background(
                                    color = PrimaryBlue,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Day name below bar (e.g. Rab)
                        Text(
                            text = point.dayName.ifBlank { point.label },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )

                        // Date number below day (e.g. 30)
                        Text(
                            text = point.label,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopProductTabletRow(item: TopProductDisplayItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank circle badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${item.rank}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(Modifier.width(10.dp))

        // Thumbnail
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate100)
                .border(0.5.dp, Slate200, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Product name & category
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                maxLines = 1
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = item.categoryName,
                fontSize = 11.5.sp,
                color = Slate500
            )
        }

        Spacer(Modifier.width(8.dp))

        // Qty sold
        Text(
            text = item.totalQtyText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
    }
}
