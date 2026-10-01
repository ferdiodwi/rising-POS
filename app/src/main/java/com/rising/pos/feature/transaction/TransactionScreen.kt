package com.rising.pos.feature.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.transaction.components.TransactionDetailDialog
import com.rising.pos.ui.components.SecurityPinDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Riwayat transaksi.
 *
 * Daftar menonjolkan nomor struk (monospace, karena itu identitas transaksi) dan
 * total (tabular). Status ditandai chip berikon supaya tidak bergantung pada warna.
 */
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Riwayat transaksi", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    text = if (searchQuery.isNotBlank() || statusFilter != null) {
                        "${transactions.size} dari ${allTransactions.size} transaksi"
                    } else {
                        "${allTransactions.size} transaksi tersimpan"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari no struk, produk, atau catatan") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = Slate500)
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilterChip("Semua", statusFilter == null) { viewModel.onStatusFilterChange(null) }
                StatusFilterChip("Selesai", statusFilter == TransactionStatus.COMPLETED) {
                    viewModel.onStatusFilterChange(TransactionStatus.COMPLETED)
                }
                StatusFilterChip("Void / Batal", statusFilter == TransactionStatus.CANCELLED) {
                    viewModel.onStatusFilterChange(TransactionStatus.CANCELLED)
                }
                StatusFilterChip("Refund", statusFilter == TransactionStatus.REFUNDED) {
                    viewModel.onStatusFilterChange(TransactionStatus.REFUNDED)
                }
                StatusFilterChip("Ditahan", statusFilter == TransactionStatus.HELD) {
                    viewModel.onStatusFilterChange(TransactionStatus.HELD)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = if (allTransactions.isEmpty()) "Belum ada transaksi" else "Transaksi tidak ditemukan",
                        description = if (allTransactions.isEmpty()) {
                            "Selesaikan pembayaran di menu Kasir. Struk dan detailnya akan tersimpan di sini."
                        } else {
                            "Coba kata kunci lain atau ubah filter status."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
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
}

@Composable
private fun StatusFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.height(40.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Slate200
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = Slate500,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
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

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trx.receiptNumber,
                    style = PosTextStyles.receiptNo,
                    color = Slate700,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(status = trx.status)
            }

            Text(
                text = itemsSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = Slate200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = CurrencyFormatter.format(trx.grandTotal, currencySymbol),
                        style = PosTextStyles.priceCard,
                        color = Slate900,
                        textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "${paymentLabel(trx.paymentMethod)} · $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }
    }
}

/** Chip status: warna + ikon + teks, bukan warna saja. */
@Composable
private fun StatusBadge(status: TransactionStatus) {
    val (container, content, icon, label) = when (status) {
        TransactionStatus.COMPLETED ->
            StatusBadgeStyle(SuccessGreenContainer, SuccessGreen, Icons.Outlined.CheckCircle, "Selesai")
        TransactionStatus.CANCELLED ->
            StatusBadgeStyle(DangerRedContainer, DangerRed, Icons.Outlined.Cancel, "Void")
        TransactionStatus.REFUNDED ->
            StatusBadgeStyle(WarningAmberContainer, WarningAmber, Icons.Outlined.Refresh, "Refund")
        TransactionStatus.HELD ->
            StatusBadgeStyle(WarningAmberContainer, WarningAmber, Icons.Outlined.PauseCircle, "Ditahan")
    }

    Surface(shape = RoundedCornerShape(999.dp), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}

private data class StatusBadgeStyle(
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val icon: ImageVector,
    val label: String
)

private fun paymentLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer bank"
    PaymentMethod.DEBIT_CARD -> "Kartu debit"
    PaymentMethod.CREDIT_CARD -> "Kartu kredit"
    PaymentMethod.E_WALLET -> "Dompet digital"
    PaymentMethod.OTHER -> "Lainnya"
}
