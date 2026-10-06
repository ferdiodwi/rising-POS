package com.rising.pos.feature.transaction.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.core.util.ReceiptFormatter
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
 * Detail transaksi, disusun seperti struk: kop toko, meta, item, ringkasan uang.
 * Aksi: bagikan, cetak, dan void (khusus transaksi selesai) dengan konfirmasi.
 */
@Composable
fun TransactionDetailDialog(
    transactionWithDetails: TransactionWithDetails,
    settings: BusinessSettings,
    isProcessing: Boolean = false,
    isPrinting: Boolean = false,
    onPrintReceipt: () -> Unit = {},
    onDismiss: () -> Unit,
    onVoidTransaction: (transactionId: String, reason: String) -> Unit,
    onRefundTransaction: (transactionId: String, reason: String) -> Unit = { _, _ -> }
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val isCompactHeight = screenHeight < 500

    val context = LocalContext.current
    val trx = transactionWithDetails.transaction
    val items = transactionWithDetails.items
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(trx.createdAt))

    var showVoidConfirmDialog by remember { mutableStateOf(false) }
    var showRefundConfirmDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(if (isCompactHeight) 14.dp else 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isCompactHeight) 16.dp else 20.dp,
                        vertical = if (isCompactHeight) 12.dp else 20.dp
                    )
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detail transaksi", style = MaterialTheme.typography.titleMedium, color = Slate900)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Status: ikon + teks
                StatusHeader(status = trx.status)

                Spacer(Modifier.height(16.dp))

                // Struk
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            text = settings.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate900,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        if (settings.address.isNotEmpty()) {
                            Text(
                                text = settings.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        ReceiptDivider()

                        ReceiptRow("No. struk", trx.receiptNumber, isMono = true)
                        ReceiptRow("Waktu", formattedDate)
                        ReceiptRow("Tipe order", orderTypeLabel(trx.orderType.name))
                        if (!trx.cashierId.isNullOrEmpty()) {
                            ReceiptRow("Kasir", trx.cashierId)
                        }
                        if (trx.splitPaymentMethod != null && trx.splitAmount > 0) {
                            ReceiptRow("Metode bayar", "Split (${paymentLabel(trx.paymentMethod)} + ${paymentLabel(trx.splitPaymentMethod)})")
                        } else {
                            ReceiptRow("Metode bayar", paymentLabel(trx.paymentMethod))
                        }

                        ReceiptDivider()

                        for (itemDetail in items) {
                            val item = itemDetail.item
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName + if (!item.variantName.isNullOrEmpty()) " (${item.variantName})" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "${item.qty.toInt()} x ${CurrencyFormatter.format(item.unitPrice, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                    for (mod in itemDetail.modifiers) {
                                        Text(
                                            text = "+ ${mod.modifierName} (${if (mod.price > 0) CurrencyFormatter.format(mod.price, settings.currencySymbol) else "Gratis"})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyFormatter.format(item.subtotal, settings.currencySymbol),
                                    style = PosTextStyles.money,
                                    color = Slate900
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }

                        ReceiptDivider()

                        ReceiptRow("Subtotal", CurrencyFormatter.format(trx.subtotal, settings.currencySymbol))
                        if (trx.discount > 0) {
                            ReceiptRow("Diskon", "- ${CurrencyFormatter.format(trx.discount, settings.currencySymbol)}")
                        }
                        if (trx.tax > 0) {
                            ReceiptRow("Pajak", CurrencyFormatter.format(trx.tax, settings.currencySymbol))
                        }
                        if (trx.serviceCharge > 0) {
                            ReceiptRow("Layanan", CurrencyFormatter.format(trx.serviceCharge, settings.currencySymbol))
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, color = Slate900)
                            Text(
                                CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol),
                                style = PosTextStyles.priceCard,
                                color = Slate900
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        if (trx.splitPaymentMethod != null && trx.splitAmount > 0) {
                            val amount1 = (trx.grandTotal - trx.splitAmount).coerceAtLeast(0L)
                            ReceiptRow("• ${paymentLabel(trx.paymentMethod)}", CurrencyFormatter.format(amount1, settings.currencySymbol))
                            ReceiptRow("• ${paymentLabel(trx.splitPaymentMethod)}", CurrencyFormatter.format(trx.splitAmount, settings.currencySymbol))
                        } else {
                            ReceiptRow("Bayar", CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol))
                            ReceiptRow("Kembalian", CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol), isGreen = true)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val receiptText = ReceiptFormatter.formatReceiptText(transactionWithDetails, settings)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan struk transaksi"))
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Bagikan", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    }

                    Button(
                        onClick = onPrintReceipt,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isPrinting && !isProcessing
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Mencetak...", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cetak", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                }

                if (trx.status == TransactionStatus.COMPLETED) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRefundConfirmDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarningAmber),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = WarningAmber
                            ),
                            enabled = !isProcessing && !isPrinting
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Refund", style = MaterialTheme.typography.labelLarge, color = WarningAmber)
                        }

                        Button(
                            onClick = { showVoidConfirmDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            enabled = !isProcessing && !isPrinting
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Void", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showVoidConfirmDialog) {
        VoidReasonConfirmDialog(
            receiptNumber = trx.receiptNumber,
            onDismiss = { showVoidConfirmDialog = false },
            onConfirmVoid = { reason ->
                showVoidConfirmDialog = false
                onVoidTransaction(trx.id, reason)
            }
        )
    }

    if (showRefundConfirmDialog) {
        RefundReasonConfirmDialog(
            receiptNumber = trx.receiptNumber,
            onDismiss = { showRefundConfirmDialog = false },
            onConfirmRefund = { reason ->
                showRefundConfirmDialog = false
                onRefundTransaction(trx.id, reason)
            }
        )
    }
}

@Composable
private fun StatusHeader(status: TransactionStatus) {
    val (container, content, icon, title, subtitle) = when (status) {
        TransactionStatus.COMPLETED -> StatusHeaderData(
            SuccessGreenContainer, SuccessGreen, Icons.Outlined.CheckCircle,
            "Transaksi selesai", "Pembayaran diterima dan stok sudah dikurangi."
        )
        TransactionStatus.CANCELLED -> StatusHeaderData(
            DangerRedContainer, DangerRed, Icons.Outlined.Cancel,
            "Transaksi dibatalkan", "Stok barang telah dikembalikan ke inventaris."
        )
        TransactionStatus.REFUNDED -> StatusHeaderData(
            WarningAmberContainer, WarningAmber, Icons.Outlined.Refresh,
            "Transaksi di-refund", "Dana telah dikembalikan ke pelanggan."
        )
        TransactionStatus.HELD -> StatusHeaderData(
            WarningAmberContainer, WarningAmber, Icons.Outlined.PauseCircle,
            "Transaksi ditahan", "Pesanan disimpan dan belum dibayar."
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(52.dp).background(container, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = content)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Slate500,
            textAlign = TextAlign.Center
        )
    }
}

private data class StatusHeaderData(
    val container: Color,
    val content: Color,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun ReceiptDivider() {
    HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun ReceiptRow(label: String, value: String, isMono: Boolean = false, isGreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(
            text = value,
            style = if (isMono) {
                PosTextStyles.receiptNo
            } else {
                PosTextStyles.money.copy(color = if (isGreen) SuccessGreen else Slate700)
            },
            color = if (isGreen) SuccessGreen else Slate700
        )
    }
}

@Composable
private fun VoidReasonConfirmDialog(
    receiptNumber: String,
    onDismiss: () -> Unit,
    onConfirmVoid: (reason: String) -> Unit
) {
    val predefinedReasons = listOf(
        "Salah input pesanan",
        "Pelanggan membatalkan pesanan",
        "Barang cacat / retur",
        "Transaksi ganda",
        "Lainnya"
    )

    var selectedReason by remember { mutableStateOf(predefinedReasons[0]) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Batalkan transaksi?", style = MaterialTheme.typography.titleMedium, color = Slate900)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transaksi $receiptNumber akan dibatalkan. Stok seluruh item yang terjual akan otomatis dikembalikan ke inventaris.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )

                Text("Alasan pembatalan", style = MaterialTheme.typography.labelLarge, color = Slate900)

                predefinedReasons.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason) },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedReason == reason) DangerRed else Slate200
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = Slate500,
                            selectedContainerColor = DangerRedContainer,
                            selectedLabelColor = DangerRed
                        )
                    )
                }

                if (selectedReason == "Lainnya") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Tuliskan alasan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Lainnya" && customReason.isNotBlank()) {
                        customReason.trim()
                    } else {
                        selectedReason
                    }
                    onConfirmVoid(finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya, batalkan", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
            }
        }
    )
}

@Composable
private fun RefundReasonConfirmDialog(
    receiptNumber: String,
    onDismiss: () -> Unit,
    onConfirmRefund: (reason: String) -> Unit
) {
    val predefinedReasons = listOf(
        "Pelanggan retur barang",
        "Barang cacat / rusak",
        "Kelebihan pembayaran",
        "Pesanan tidak sesuai",
        "Lainnya"
    )

    var selectedReason by remember { mutableStateOf(predefinedReasons[0]) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Refund transaksi?", style = MaterialTheme.typography.titleMedium, color = Slate900)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transaksi $receiptNumber akan di-refund. Dana dikembalikan ke pelanggan dan stok seluruh item akan otomatis dikembalikan ke inventaris.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )

                Text("Alasan refund", style = MaterialTheme.typography.labelLarge, color = Slate900)

                predefinedReasons.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason) },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedReason == reason) WarningAmber else Slate200
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = Slate500,
                            selectedContainerColor = WarningAmberContainer,
                            selectedLabelColor = WarningAmber
                        )
                    )
                }

                if (selectedReason == "Lainnya") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Tuliskan alasan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Lainnya" && customReason.isNotBlank()) {
                        customReason.trim()
                    } else {
                        selectedReason
                    }
                    onConfirmRefund(finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya, refund", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
            }
        }
    )
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

private fun orderTypeLabel(name: String): String = when (name) {
    "DINE_IN" -> "Makan di tempat"
    "TAKEAWAY" -> "Bungkus"
    "DELIVERY" -> "Diantar"
    "RETAIL" -> "Retail"
    else -> name
}
