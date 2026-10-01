package com.rising.pos.feature.transaction

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.ui.components.WorkspaceHeader
import com.rising.pos.ui.components.WorkspaceEmptyState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextDecoration
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.feature.transaction.components.TransactionDetailDialog
import com.rising.pos.ui.components.SecurityPinDialog
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.WarningAmber

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingVoidAction by remember { mutableStateOf<Pair<String, String>?>(null) }

    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            WorkspaceHeader(
                "Riwayat transaksi",
                if (searchQuery.isNotBlank() || statusFilter != null) "${transactions.size} dari ${allTransactions.size} transaksi"
                else "${allTransactions.size} transaksi tersimpan"
            )
            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari no struk, produk, atau catatan...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.onStatusFilterChange(null) },
                    label = { Text("Semua", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == TransactionStatus.COMPLETED,
                    onClick = { viewModel.onStatusFilterChange(TransactionStatus.COMPLETED) },
                    label = { Text("Selesai", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == TransactionStatus.CANCELLED,
                    onClick = { viewModel.onStatusFilterChange(TransactionStatus.CANCELLED) },
                    label = { Text("Void / Batal", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == TransactionStatus.REFUNDED,
                    onClick = { viewModel.onStatusFilterChange(TransactionStatus.REFUNDED) },
                    label = { Text("Refund", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == TransactionStatus.HELD,
                    onClick = { viewModel.onStatusFilterChange(TransactionStatus.HELD) },
                    label = { Text("Ditahan", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        if (allTransactions.isEmpty()) "Belum ada transaksi" else "Transaksi tidak ditemukan",
                        if (allTransactions.isEmpty()) "Selesaikan pembayaran di menu Kasir. Struk dan detailnya akan tersimpan di sini."
                        else "Coba kata kunci lain atau ubah filter status."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { item ->
                        TransactionItemCard(
                            trxDetails = item,
                            currencySymbol = settings.currencySymbol,
                            dateStr = dateFormatter.format(Date(item.transaction.createdAt)),
                            onClick = { viewModel.selectTransaction(item) }
                        )
                    }
                }
            }
        }
    }

    // Receipt Detail & Void Dialog
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

    if (showPinDialog && pendingVoidAction != null) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi Void",
            description = "Masukkan PIN Owner untuk menyetujui pembatalan transaksi ini.",
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
}

@Composable
private fun TransactionItemCard(
    trxDetails: TransactionWithDetails,
    currencySymbol: String,
    dateStr: String,
    onClick: () -> Unit
) {
    val trx = trxDetails.transaction
    val itemsSummary = trxDetails.items.joinToString(", ") { "${it.item.productName} (${it.item.qty.toInt()})" }
    val isVoided = trx.status == TransactionStatus.CANCELLED
    val isRefunded = trx.status == TransactionStatus.REFUNDED

    val paymentLabel = when (trx.paymentMethod) {
        PaymentMethod.CASH -> "Tunai"
        PaymentMethod.QRIS -> "QRIS"
        PaymentMethod.BANK_TRANSFER -> "Transfer bank"
        PaymentMethod.DEBIT_CARD -> "Kartu debit"
        PaymentMethod.CREDIT_CARD -> "Kartu kredit"
        PaymentMethod.E_WALLET -> "Dompet digital"
        PaymentMethod.OTHER -> "Lainnya"
    }
    val statusLabel = when (trx.status) {
        TransactionStatus.COMPLETED -> "Selesai"
        TransactionStatus.CANCELLED -> "Dibatalkan"
        TransactionStatus.REFUNDED -> "Dikembalikan"
        TransactionStatus.HELD -> "Tertunda"
    }
    val statusColor = when (trx.status) {
        TransactionStatus.CANCELLED -> DangerRed
        TransactionStatus.REFUNDED, TransactionStatus.HELD -> WarningAmber
        else -> SuccessGreen
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Slate500, modifier = Modifier.weight(1f))
                Text(statusLabel, style = MaterialTheme.typography.labelMedium, color = statusColor)
            }
            Text(trx.receiptNumber, style = MaterialTheme.typography.titleSmall, color = Slate900)
            Text(itemsSummary, style = MaterialTheme.typography.bodyMedium, color = Slate500,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            androidx.compose.material3.HorizontalDivider(color = Slate200)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(CurrencyFormatter.format(trx.grandTotal, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
                        ), color = Slate900)
                    Text(paymentLabel, style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Slate500, modifier = Modifier.size(20.dp))
            }
        }
    }
}
