package com.rising.pos.feature.transaction.components

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.core.util.ReceiptFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    transactionWithDetails: TransactionWithDetails,
    settings: BusinessSettings,
    isProcessing: Boolean = false,
    isPrinting: Boolean = false,
    onPrintReceipt: () -> Unit = {},
    onDismiss: () -> Unit,
    onVoidTransaction: (transactionId: String, reason: String) -> Unit
) {
    val context = LocalContext.current
    val trx = transactionWithDetails.transaction
    val items = transactionWithDetails.items
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(trx.createdAt))

    var showVoidConfirmDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Badge & Icon
                val (badgeBg, badgeText, statusIcon) = when (trx.status) {
                    TransactionStatus.COMPLETED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, Icons.Default.CheckCircle)
                    TransactionStatus.CANCELLED -> Triple(DangerRed.copy(alpha = 0.15f), DangerRed, Icons.Default.Cancel)
                    TransactionStatus.REFUNDED -> Triple(WarningAmber.copy(alpha = 0.15f), WarningAmber, Icons.Default.Warning)
                    TransactionStatus.HELD -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, Icons.Default.Info)
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(badgeBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = badgeText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (trx.status) {
                        TransactionStatus.COMPLETED -> "Transaksi Selesai"
                        TransactionStatus.CANCELLED -> "Transaksi Dibatalkan (VOID)"
                        TransactionStatus.REFUNDED -> "Transaksi Di-refund"
                        TransactionStatus.HELD -> "Transaksi Tertunda"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                )

                if (trx.status == TransactionStatus.CANCELLED) {
                    Text(
                        text = "Stok barang telah dikembalikan ke inventaris.",
                        style = MaterialTheme.typography.labelSmall.copy(color = DangerRed),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Receipt Paper Container
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate200.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Store Header
                        Text(
                            text = settings.name.uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        if (settings.address.isNotEmpty()) {
                            Text(
                                text = settings.address,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }


                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate200, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Receipt Details
                        ReceiptRow("No. Struk", trx.receiptNumber, isMono = true)
                        ReceiptRow("Waktu", formattedDate)
                        ReceiptRow("Tipe Order", trx.orderType.name)
                        if (!trx.cashierId.isNullOrEmpty()) {
                            ReceiptRow("Kasir", trx.cashierId)
                        }
                        ReceiptRow("Metode Bayar", trx.paymentMethod.name)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate200, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items
                        for (itemDetail in items) {
                            val item = itemDetail.item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName + if (!item.variantName.isNullOrEmpty()) " (${item.variantName})" else "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Slate900)
                                    )
                                    Text(
                                        text = "${item.qty.toInt()} x ${CurrencyFormatter.format(item.unitPrice, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                    )
                                    for (mod in itemDetail.modifiers) {
                                        Text(
                                            text = " + ${mod.modifierName} (${if (mod.price > 0) CurrencyFormatter.format(mod.price, settings.currencySymbol) else "Gratis"})",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyFormatter.format(item.subtotal, settings.currencySymbol),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        HorizontalDivider(color = Slate200, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Totals
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

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                            Text(CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        ReceiptRow("Bayar", CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol))
                        ReceiptRow("Kembalian", CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol), isGreen = true)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share Receipt
                    OutlinedButton(
                        onClick = {
                            val receiptText = ReceiptFormatter.formatReceiptText(transactionWithDetails, settings)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Transaksi")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", fontSize = 13.sp)
                    }

                    // Print Thermal Receipt
                    Button(
                        onClick = onPrintReceipt,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isPrinting && !isProcessing
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mencetak...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak", fontSize = 13.sp)
                        }
                    }
                }

                // Void button (only if transaction is completed)
                if (trx.status == TransactionStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showVoidConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        enabled = !isProcessing && !isPrinting
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Batalkan Transaksi (Void)", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Voiding
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
}

@Composable
private fun ReceiptRow(label: String, value: String, isMono: Boolean = false, isGreen: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isGreen) FontWeight.Bold else FontWeight.Medium,
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
                color = if (isGreen) SuccessGreen else Slate700
            )
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Batalkan Transaksi (VOID)?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transaksi #$receiptNumber akan dibatalkan. Stok seluruh item produk yang terjual akan otomatis dikembalikan ke inventaris.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                )

                Text(
                    text = "Pilih Alasan Pembatalan:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )

                predefinedReasons.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DangerRed.copy(alpha = 0.15f),
                            selectedLabelColor = DangerRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedReason == "Lainnya") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Tuliskan alasan...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ya, Batalkan Transaksi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Slate700)
            }
        }
    )
}
