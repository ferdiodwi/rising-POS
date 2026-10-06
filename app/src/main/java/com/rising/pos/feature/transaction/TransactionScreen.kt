package com.rising.pos.feature.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.transaction.components.TransactionDetailDialog
import com.rising.pos.ui.components.SecurityPinDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate800
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.Slate50
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.WarningAmber
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.PrimaryBlueContainer

// Slate* berasal dari token adaptif sehingga mengikuti mode terang/gelap.
private val BrandBlue = PrimaryBlue
private val BrandBlueBg = PrimaryBlueContainer
private val BrandBlueBorder = Color(0xFFBFDBFE)

/**
 * Layar Riwayat Transaksi sesuai style mockup:
 * - Header: "Riwayat" (28sp Bold) + Subtitle Nama Toko
 * - Search bar: "Cari nomor struk" rounded 12dp
 * - Date chips: [ Hari ini ], [ 7 hari ], [ 📅 Pilih tanggal ]
 * - Stats Summary Card: "Penjualan hari ini" | "Transaksi selesai" (tanpa menghitung batal)
 * - Section Date Header: "Hari ini" & Tanggal hari ini
 * - List transaksi: Ikon struk, No. Struk tebal, Jam • Metode, Jumlah barang / "Dibatalkan", Total Rp, dan Chevron
 * - Footer: "Semua transaksi hari ini sudah ditampilkan."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val period by viewModel.period.collectAsState()
    val customDate by viewModel.customDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingVoidAction by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingRefundAction by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Hitung penjualan transaksi yang sukses (COMPLETED)
    val completedTransactions = remember(allTransactions) {
        allTransactions.filter { it.transaction.status == TransactionStatus.COMPLETED }
    }
    val totalCompletedSales = remember(completedTransactions) {
        completedTransactions.sumOf { it.transaction.grandTotal }
    }
    val completedCount = remember(completedTransactions) {
        completedTransactions.size
    }

    val timeFormatter = remember { SimpleDateFormat("HH.mm", Locale.getDefault()) }
    val todayDateFormatted = remember {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val isWideScreen = screenWidth >= 600
    val isCompactHeight = screenHeight < 500

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val context = LocalContext.current
            fun shareReceipt(trx: TransactionWithDetails) {
                val dateText = SimpleDateFormat("d MMMM yyyy • HH.mm", Locale.forLanguageTag("id-ID")).format(Date(trx.transaction.createdAt))
                val text = buildString {
                    appendLine("STRUK PEMBELIAN - ${settings.name.ifBlank { "Rising Studio" }}")
                    appendLine("#${trx.transaction.receiptNumber}")
                    appendLine(dateText)
                    appendLine("--------------------------------")
                    trx.items.forEach { item ->
                        appendLine("${item.item.productName} (${item.item.qty.toInt()} x ${CurrencyFormatter.format(item.item.unitPrice, settings.currencySymbol)}) = ${CurrencyFormatter.format(item.item.subtotal, settings.currencySymbol)}")
                    }
                    appendLine("--------------------------------")
                    appendLine("Subtotal: ${CurrencyFormatter.format(trx.transaction.subtotal, settings.currencySymbol)}")
                    if (trx.transaction.discount > 0) {
                        appendLine("Diskon: ${CurrencyFormatter.format(trx.transaction.discount, settings.currencySymbol)}")
                    }
                    appendLine("Total: ${CurrencyFormatter.format(trx.transaction.grandTotal, settings.currencySymbol)}")
                    appendLine("Metode: ${paymentLabel(trx.transaction.paymentMethod)}")
                    appendLine("Bayar: ${CurrencyFormatter.format(trx.transaction.paymentAmount, settings.currencySymbol)}")
                    appendLine("Kembali: ${CurrencyFormatter.format(trx.transaction.changeAmount, settings.currencySymbol)}")
                    if (!settings.footerNote.isNullOrBlank()) {
                        appendLine()
                        appendLine(settings.footerNote)
                    }
                }
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Struk"))
            }

            fun exportAllTransactions() {
                val text = buildString {
                    appendLine("No Struk,Waktu,Metode,Total,Status")
                    transactions.forEach { t ->
                        val time = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(t.transaction.createdAt))
                        appendLine("${t.transaction.receiptNumber},$time,${paymentLabel(t.transaction.paymentMethod)},${t.transaction.grandTotal},${t.transaction.status}")
                    }
                }
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/csv"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Ekspor Transaksi"))
            }

            val customDateText = remember(customDate, period) {
                if (customDate != null) {
                    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id-ID")).format(customDate)
                } else if (period == HistoryPeriod.ALL) {
                    "Semua"
                } else {
                    "Pilih tanggal"
                }
            }

            // ── Top Header Row ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (isCompactHeight) 14.dp else 20.dp,
                        end = if (isCompactHeight) 14.dp else 20.dp,
                        top = if (isCompactHeight) 6.dp else 12.dp,
                        bottom = if (isCompactHeight) 4.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.rising.pos.ui.components.PosHeaderTitleSection(
                    title = "Riwayat",
                    subtitle = settings.name.ifBlank { "Rising Studio" },
                    modifier = Modifier.weight(1f)
                )

                com.rising.pos.ui.components.PosHeaderActionButton(
                    icon = Icons.Outlined.FileDownload,
                    contentDescription = "Ekspor Struk & Transaksi",
                    onClick = ::exportAllTransactions
                )
            }

            if (isWideScreen && !isCompactHeight) {
                TransactionTabletMasterDetail(
                    transactions = transactions,
                    totalCompletedSales = totalCompletedSales,
                    completedCount = completedCount,
                    period = period,
                    customDate = customDate,
                    customDateText = customDateText,
                    todayDateFormatted = todayDateFormatted,
                    currencySymbol = settings.currencySymbol,
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onSelectPeriod = {
                        viewModel.setCustomDate(null)
                        viewModel.setPeriod(it)
                    },
                    onPickCustomDate = { showDatePicker = true },
                    onPrintReceipt = viewModel::printReceipt,
                    onShareReceipt = ::shareReceipt,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            } else {
                // ── Phone Portrait: Search Bar ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    com.rising.pos.ui.components.PosSearchBar(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = "Cari nomor struk"
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Date Filter Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = "Hari ini",
                        selected = period == HistoryPeriod.TODAY && customDate == null,
                        onClick = {
                            viewModel.setCustomDate(null)
                            viewModel.setPeriod(HistoryPeriod.TODAY)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = "7 hari",
                        selected = period == HistoryPeriod.LAST_7_DAYS && customDate == null,
                        onClick = {
                            viewModel.setCustomDate(null)
                            viewModel.setPeriod(HistoryPeriod.LAST_7_DAYS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    com.rising.pos.ui.components.PosDateFilterButton(
                        text = customDateText,
                        selected = customDate != null || period == HistoryPeriod.ALL,
                        icon = Icons.Outlined.CalendarToday,
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Stats Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    customDate != null -> "Penjualan tanggal ini"
                                    period == HistoryPeriod.TODAY -> "Penjualan hari ini"
                                    period == HistoryPeriod.LAST_7_DAYS -> "Penjualan 7 hari"
                                    else -> "Total penjualan"
                                },
                                style = TextStyle(fontSize = 12.5.sp, color = Slate500)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.format(totalCompletedSales, settings.currencySymbol),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(Slate200)
                        )

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(
                                text = "Transaksi selesai",
                                style = TextStyle(fontSize = 12.5.sp, color = Slate500)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$completedCount",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }
                    }
                }

                if (!isCompactHeight) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Transaksi batal tidak dihitung.",
                        style = TextStyle(fontSize = 11.5.sp, color = Slate400),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }

                // Section Header: "Hari ini" & Tanggal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            top = if (isCompactHeight) 6.dp else 14.dp,
                            bottom = if (isCompactHeight) 4.dp else 8.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            customDate != null -> DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(customDate)
                            period == HistoryPeriod.TODAY -> "Hari ini"
                            period == HistoryPeriod.LAST_7_DAYS -> "7 hari terakhir"
                            else -> "Semua transaksi"
                        },
                        style = TextStyle(
                            fontSize = if (isCompactHeight) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )

                    Text(
                        text = todayDateFormatted,
                        style = TextStyle(
                            fontSize = if (isCompactHeight) 11.5.sp else 13.sp,
                            color = Slate500
                        )
                    )
                }

                // List Transaksi (Phone)
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WorkspaceEmptyState(
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            title = if (allTransactions.isEmpty()) "Belum ada transaksi" else "Transaksi tidak ditemukan",
                            description = if (allTransactions.isEmpty()) {
                                "Selesaikan pembayaran di menu Kasir. Struk dan detailnya akan tersimpan di sini."
                            } else {
                                "Coba kata kunci nomor struk lain atau ubah filter tanggal."
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(transactions, key = { it.transaction.id }) { item ->
                            TransactionRowItem(
                                trxDetails = item,
                                currencySymbol = settings.currencySymbol,
                                timeStr = timeFormatter.format(Date(item.transaction.createdAt)),
                                onClick = { viewModel.selectTransaction(item) }
                            )
                            HorizontalDivider(color = Slate100, thickness = 1.dp)
                        }

                        item {
                            Text(
                                text = if (period == HistoryPeriod.TODAY && customDate == null) {
                                    "Semua transaksi hari ini sudah ditampilkan."
                                } else {
                                    "Semua transaksi sudah ditampilkan."
                                },
                                style = TextStyle(fontSize = 12.sp, color = Slate400),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Detail Struk / Void / Cetak ────────────────────────────────────
    uiState.selectedTransaction?.let { trx ->
        TransactionDetailDialog(
            transactionWithDetails = trx,
            settings = settings,
            isProcessing = uiState.isProcessing,
            isPrinting = uiState.isPrinting,
            onPrintReceipt = { viewModel.printReceipt(trx) },
            onDismiss = { viewModel.selectTransaction(null) },
            onVoidTransaction = { trxId, reason ->
                if (settings.isPinSecurityEnabled && settings.hasPin) {
                    pendingVoidAction = Pair(trxId, reason)
                    showPinDialog = true
                } else {
                    viewModel.voidTransaction(trxId, reason)
                }
            },
            onRefundTransaction = { trxId, reason ->
                if (settings.isPinSecurityEnabled && settings.hasPin) {
                    pendingRefundAction = Pair(trxId, reason)
                    showPinDialog = true
                } else {
                    viewModel.refundTransaction(trxId, reason)
                }
            }
        )
    }

    // ── Dialog Otorisasi PIN untuk Void / Refund Transaksi ─────────────────────
    if (showPinDialog && (pendingVoidAction != null || pendingRefundAction != null)) {
        val isRefund = pendingRefundAction != null
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = if (isRefund) "Otorisasi Refund" else "Otorisasi Void",
            description = if (isRefund) {
                "Masukkan PIN owner untuk menyetujui refund transaksi ini."
            } else {
                "Masukkan PIN owner untuk menyetujui pembatalan transaksi ini."
            },
            onDismiss = {
                showPinDialog = false
                pendingVoidAction = null
                pendingRefundAction = null
            },
            onSuccess = {
                val voidAction = pendingVoidAction
                val refundAction = pendingRefundAction
                showPinDialog = false
                pendingVoidAction = null
                pendingRefundAction = null
                voidAction?.let { (trxId, reason) ->
                    viewModel.voidTransaction(trxId, reason)
                }
                refundAction?.let { (trxId, reason) ->
                    viewModel.refundTransaction(trxId, reason)
                }
            }
        )
    }

    // ── Date Picker Dialog Material 3 untuk "Pilih tanggal" ───────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setCustomDate(localDate)
                        }
                    }
                ) {
                    Text("Pilih", color = BrandBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        viewModel.setCustomDate(null)
                        viewModel.setPeriod(HistoryPeriod.ALL)
                    }
                ) {
                    Text("Semua Tanggal", color = Slate700)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Tombol filter tanggal sesuai mockup:
 * - Aktif: Latar #EFF6FF, border #BFDBFE, teks & ikon #2563EB
 * - Tidak aktif: Latar putih, border #E2E8F0, teks & ikon #475569
 */
@Composable
private fun DateFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val bgColor = if (selected) BrandBlueBg else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) BrandBlueBorder else Slate200
    val contentColor = if (selected) BrandBlue else Slate600

    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 13.5.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Baris item riwayat transaksi sesuai mockup:
 * - Sisi kiri: Ikon struk receipt outline
 * - Tengah:
 *     - No Struk tebal (mis. A01-0006)
 *     - Jam • Metode pembayaran (mis. 14.15 • Tunai)
 *     - Jumlah barang (mis. 3 barang) atau "Dibatalkan" warna merah jika void
 * - Sisi kanan: Total Rp27.000 tebal dan panah chevron kanan
 */
@Composable
private fun TransactionRowItem(
    trxDetails: TransactionWithDetails,
    currencySymbol: String,
    timeStr: String,
    onClick: () -> Unit
) {
    val trx = trxDetails.transaction
    val isVoided = trx.status == TransactionStatus.CANCELLED
    val isRefunded = trx.status == TransactionStatus.REFUNDED
    val isCancelledOrRefunded = isVoided || isRefunded
    val totalQty = trxDetails.items.sumOf { it.item.qty.toInt() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon Struk
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        // Informasi Struk
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trx.receiptNumber,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (trx.splitPaymentMethod != null && trx.splitAmount > 0) {
                    "$timeStr • ${paymentLabel(trx.paymentMethod)} + ${paymentLabel(trx.splitPaymentMethod)}"
                } else {
                    "$timeStr • ${paymentLabel(trx.paymentMethod)}"
                },
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Slate500
                )
            )

            Spacer(Modifier.height(2.dp))

            if (isVoided) {
                Text(
                    text = "Dibatalkan",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DangerRed
                    )
                )
            } else if (isRefunded) {
                Text(
                    text = "Di-refund",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarningAmber
                    )
                )
            } else {
                Text(
                    text = "$totalQty barang",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Slate500
                    )
                )
            }
        }

        // Total Tagihan & Chevron
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = CurrencyFormatter.format(trx.grandTotal, currencySymbol),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelledOrRefunded) Slate400 else Slate900,
                    textDecoration = if (isCancelledOrRefunded) TextDecoration.LineThrough else TextDecoration.None
                )
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun paymentLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer"
    PaymentMethod.DEBIT_CARD -> "Debit"
    PaymentMethod.CREDIT_CARD -> "Kredit"
    PaymentMethod.E_WALLET -> "E-Wallet"
    PaymentMethod.OTHER -> "Lainnya"
}

@Composable
private fun TransactionTabletMasterDetail(
    transactions: List<TransactionWithDetails>,
    totalCompletedSales: Long,
    completedCount: Int,
    period: HistoryPeriod,
    customDate: LocalDate?,
    customDateText: String,
    todayDateFormatted: String,
    currencySymbol: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectPeriod: (HistoryPeriod) -> Unit,
    onPickCustomDate: () -> Unit,
    onPrintReceipt: (TransactionWithDetails) -> Unit,
    onShareReceipt: (TransactionWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTransactionId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = remember(transactions, selectedTransactionId) {
        transactions.firstOrNull { it.transaction.id == selectedTransactionId }
            ?: transactions.firstOrNull()
    }

    val pageSize = 6
    var currentPage by rememberSaveable { mutableStateOf(0) }
    val totalPages = maxOf(1, ((transactions.size + pageSize - 1) / pageSize))
    val safePage = currentPage.coerceIn(0, totalPages - 1)
    val pagedTransactions = remember(transactions, safePage) {
        transactions.drop(safePage * pageSize).take(pageSize)
    }
    val timeFormatter = remember { SimpleDateFormat("HH.mm", Locale.getDefault()) }

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Panel Kiri: Tabel Transaksi (Master) ───────────────────────────
        Card(
            modifier = Modifier
                .weight(1.58f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar
                com.rising.pos.ui.components.PosSearchBar(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Cari nomor struk"
                )

                Spacer(Modifier.height(10.dp))

                // Date Filter Buttons + Date indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        com.rising.pos.ui.components.PosDateFilterButton(
                            text = "Hari ini",
                            selected = period == HistoryPeriod.TODAY && customDate == null,
                            onClick = { onSelectPeriod(HistoryPeriod.TODAY) }
                        )
                        com.rising.pos.ui.components.PosDateFilterButton(
                            text = "7 hari",
                            selected = period == HistoryPeriod.LAST_7_DAYS && customDate == null,
                            onClick = { onSelectPeriod(HistoryPeriod.LAST_7_DAYS) }
                        )
                        com.rising.pos.ui.components.PosDateFilterButton(
                            text = customDateText,
                            selected = customDate != null || period == HistoryPeriod.ALL,
                            icon = Icons.Outlined.CalendarToday,
                            onClick = onPickCustomDate
                        )
                    }

                    Text(
                        text = todayDateFormatted,
                        style = TextStyle(fontSize = 13.sp, color = Slate500),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Stats Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Penjualan hari ini",
                                style = TextStyle(fontSize = 11.5.sp, color = Slate500)
                            )
                            Text(
                                text = CurrencyFormatter.format(totalCompletedSales, currencySymbol),
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Slate200))
                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transaksi selesai",
                                style = TextStyle(fontSize = 11.5.sp, color = Slate500)
                            )
                            Text(
                                text = "$completedCount",
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            )
                            Text(
                                text = "Transaksi batal tidak dihitung.",
                                style = TextStyle(fontSize = 10.5.sp, color = Slate400)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = when {
                        customDate != null -> DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(customDate)
                        period == HistoryPeriod.TODAY -> "Hari ini"
                        period == HistoryPeriod.LAST_7_DAYS -> "7 hari terakhir"
                        else -> "Semua transaksi"
                    },
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Table Header
                Surface(
                    color = Slate100.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No. struk", modifier = Modifier.weight(1.8f), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600))
                        Text("Waktu", modifier = Modifier.weight(1f), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600))
                        Text("Metode", modifier = Modifier.weight(1.2f), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600))
                        Text("Total", modifier = Modifier.weight(1.4f), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600))
                        Text("Status", modifier = Modifier.weight(1f), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate600))
                    }
                }

                // Table Rows
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WorkspaceEmptyState(
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            title = "Belum ada transaksi",
                            description = "Selesaikan pembayaran di kasir atau ubah filter pencarian."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(pagedTransactions, key = { it.transaction.id }) { item ->
                            val isSelected = (item.transaction.id == selectedItem?.transaction?.id)
                            val isCancelledOrRefunded = item.transaction.status == TransactionStatus.CANCELLED ||
                                item.transaction.status == TransactionStatus.REFUNDED

                            Surface(
                                onClick = { selectedTransactionId = item.transaction.id },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.2.dp, Color(0xFF93C5FD)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Kolom No. Struk (Icon + Nomor)
                                    Row(
                                        modifier = Modifier.weight(1.8f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryBlue else Slate500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        val displayNum = if (item.transaction.receiptNumber.startsWith("#")) {
                                            item.transaction.receiptNumber
                                        } else {
                                            "#TRX-${item.transaction.receiptNumber.takeLast(4)}"
                                        }
                                        Text(
                                            text = displayNum,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) PrimaryBlue else Slate900
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Kolom Waktu
                                    Text(
                                        text = timeFormatter.format(Date(item.transaction.createdAt)),
                                        modifier = Modifier.weight(1f),
                                        style = TextStyle(fontSize = 12.5.sp, color = Slate600)
                                    )

                                    // Kolom Metode
                                    Text(
                                        text = paymentLabel(item.transaction.paymentMethod),
                                        modifier = Modifier.weight(1.2f),
                                        style = TextStyle(fontSize = 12.5.sp, color = Slate700)
                                    )

                                    // Kolom Total
                                    Text(
                                        text = CurrencyFormatter.format(item.transaction.grandTotal, currencySymbol),
                                        modifier = Modifier.weight(1.4f),
                                        style = TextStyle(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCancelledOrRefunded) Slate400 else Slate900,
                                            textDecoration = if (isCancelledOrRefunded) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    )

                                    // Kolom Status (Badge)
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (isCancelledOrRefunded) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = "Batal",
                                                    style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626)),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "Selesai",
                                                    style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A)),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Slate100, thickness = 0.8.dp)
                        }
                    }
                }

                // Table Footer: Counter & Pagination
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val startIdx = if (transactions.isEmpty()) 0 else safePage * pageSize + 1
                    val endIdx = minOf((safePage + 1) * pageSize, transactions.size)
                    Text(
                        text = "Menampilkan $startIdx–$endIdx dari ${transactions.size} transaksi",
                        style = TextStyle(fontSize = 12.sp, color = Slate500)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                contentDescription = "Sebelumnya",
                                tint = if (currentPage > 0) Slate700 else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        val maxShownPages = minOf(6, totalPages)
                        for (p in 0 until maxShownPages) {
                            val isCurrent = (p == safePage)
                            Surface(
                                onClick = { currentPage = p },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) PrimaryBlue else Color.Transparent,
                                border = if (!isCurrent) BorderStroke(1.dp, Slate200) else null,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${p + 1}",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) Color.White else Slate700
                                        )
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Berikutnya",
                                tint = if (currentPage < totalPages - 1) Slate700 else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Panel Kanan: Detail Transaksi ──────────────────────────────────
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (selectedItem != null) {
                val isCancelledOrRefunded = selectedItem.transaction.status == TransactionStatus.CANCELLED ||
                    selectedItem.transaction.status == TransactionStatus.REFUNDED

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header Detail
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Detail transaksi",
                            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCancelledOrRefunded) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (isCancelledOrRefunded) "Batal" else "Selesai",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCancelledOrRefunded) Color(0xFFDC2626) else Color(0xFF16A34A)
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val displayNum = if (selectedItem.transaction.receiptNumber.startsWith("#")) {
                        selectedItem.transaction.receiptNumber
                    } else {
                        "#TRX-${selectedItem.transaction.receiptNumber.takeLast(4)}"
                    }
                    Text(
                        text = displayNum,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    )
                    val dateFormatted = SimpleDateFormat("d MMMM yyyy • HH.mm", Locale.forLanguageTag("id-ID")).format(Date(selectedItem.transaction.createdAt))
                    Text(
                        text = dateFormatted,
                        style = TextStyle(fontSize = 12.5.sp, color = Slate400)
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Slate100, thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    // Items list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(selectedItem.items) { item ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.item.productName,
                                        style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(item.item.subtotal, currencySymbol),
                                        style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    )
                                }
                                Text(
                                    text = "${item.item.qty.toInt()} × ${CurrencyFormatter.format(item.item.unitPrice, currencySymbol)}",
                                    style = TextStyle(fontSize = 12.sp, color = Slate400)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Slate100, thickness = 1.dp)
                    Spacer(Modifier.height(10.dp))

                    // Financial Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = TextStyle(fontSize = 13.sp, color = Slate500))
                            Text(CurrencyFormatter.format(selectedItem.transaction.subtotal, currencySymbol), style = TextStyle(fontSize = 13.5.sp, color = Slate800))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", style = TextStyle(fontSize = 13.sp, color = Slate500))
                            Text(CurrencyFormatter.format(selectedItem.transaction.discount, currencySymbol), style = TextStyle(fontSize = 13.5.sp, color = Slate800))
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900))
                            Text(CurrencyFormatter.format(selectedItem.transaction.grandTotal, currencySymbol), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900))
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Metode pembayaran", style = TextStyle(fontSize = 12.5.sp, color = Slate500))
                            Text(paymentLabel(selectedItem.transaction.paymentMethod), style = TextStyle(fontSize = 13.sp, color = Slate700))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Uang diterima", style = TextStyle(fontSize = 12.5.sp, color = Slate500))
                            Text(CurrencyFormatter.format(selectedItem.transaction.paymentAmount, currencySymbol), style = TextStyle(fontSize = 13.sp, color = Slate700))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kembalian", style = TextStyle(fontSize = 12.5.sp, color = Slate500))
                            Text(CurrencyFormatter.format(selectedItem.transaction.changeAmount, currencySymbol), style = TextStyle(fontSize = 13.sp, color = Slate700))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action Buttons
                    Button(
                        onClick = { onPrintReceipt(selectedItem) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cetak struk", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onShareReceipt(selectedItem) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Bagikan struk", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pilih transaksi untuk melihat detail",
                        style = TextStyle(fontSize = 14.sp, color = Slate400)
                    )
                }
            }
        }
    }
}

