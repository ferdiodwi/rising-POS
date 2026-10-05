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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.Slate50
import com.rising.pos.ui.theme.DangerRed
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
            // ── Top Header Row ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Riwayat",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = settings.name.ifBlank { "Warung Bu Siti" },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Slate500
                    )
                )
            }

            // ── Search Bar: "Cari nomor struk" ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Cari nomor struk",
                            style = TextStyle(fontSize = 14.sp, color = Slate400)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus pencarian",
                                    tint = Slate500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = Slate50,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Date Filter Buttons: [ Hari ini ] [ 7 hari ] [ 📅 Pilih tanggal ] ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Hari ini
                DateFilterButton(
                    text = "Hari ini",
                    selected = period == HistoryPeriod.TODAY && customDate == null,
                    onClick = {
                        viewModel.setCustomDate(null)
                        viewModel.setPeriod(HistoryPeriod.TODAY)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Button 2: 7 hari
                DateFilterButton(
                    text = "7 hari",
                    selected = period == HistoryPeriod.LAST_7_DAYS && customDate == null,
                    onClick = {
                        viewModel.setCustomDate(null)
                        viewModel.setPeriod(HistoryPeriod.LAST_7_DAYS)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Button 3: Pilih tanggal
                val customDateText = remember(customDate, period) {
                    if (customDate != null) {
                        DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id-ID")).format(customDate)
                    } else if (period == HistoryPeriod.ALL) {
                        "Semua"
                    } else {
                        "Pilih tanggal"
                    }
                }
                DateFilterButton(
                    text = customDateText,
                    selected = customDate != null || period == HistoryPeriod.ALL,
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Summary Stats Card: "Penjualan hari ini" | "Transaksi selesai" ─
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom Kiri
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                customDate != null -> "Penjualan tanggal ini"
                                period == HistoryPeriod.TODAY -> "Penjualan hari ini"
                                period == HistoryPeriod.LAST_7_DAYS -> "Penjualan 7 hari"
                                else -> "Total penjualan"
                            },
                            style = TextStyle(fontSize = 13.sp, color = Slate500)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.format(totalCompletedSales, settings.currencySymbol),
                            style = TextStyle(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }

                    // Garis Pembatas Vertikal
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(42.dp)
                            .background(Slate200)
                    )

                    Spacer(Modifier.width(16.dp))

                    // Kolom Kanan
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Transaksi selesai",
                            style = TextStyle(fontSize = 13.sp, color = Slate500)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$completedCount",
                            style = TextStyle(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Transaksi batal tidak dihitung.",
                style = TextStyle(fontSize = 12.sp, color = Slate400),
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            // ── Section Header Row: "Hari ini" & Tanggal ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 8.dp),
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )

                Text(
                    text = todayDateFormatted,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Slate500
                    )
                )
            }

            // ── Daftar Transaksi Sesuai Mockup ────────────────────────────────
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
                                .padding(vertical = 24.dp),
                            textAlign = TextAlign.Center
                        )
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
            }
        )
    }

    // ── Dialog Otorisasi PIN untuk Void Transaksi ─────────────────────────────
    if (showPinDialog && pendingVoidAction != null) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi Void",
            description = "Masukkan PIN owner untuk menyetujui pembatalan transaksi ini.",
            onDismiss = {
                showPinDialog = false
                pendingVoidAction = null
            },
            onSuccess = {
                val action = pendingVoidAction
                showPinDialog = false
                pendingVoidAction = null
                action?.let { (trxId, reason) ->
                    viewModel.voidTransaction(trxId, reason)
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
                text = "$timeStr • ${paymentLabel(trx.paymentMethod)}",
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
                    color = if (isVoided) Slate400 else Slate900,
                    textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
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
