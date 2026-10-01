package com.rising.pos.feature.pos.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.core.util.ReceiptFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Konfirmasi transaksi berhasil.
 *
 * Menampilkan pratinjau struk (motif "bahasa struk": nomor struk monospace,
 * nominal tabular) lalu aksi cetak, bagikan, atau mulai transaksi baru.
 */
@Composable
fun ReceiptSuccessDialog(
    transactionWithDetails: TransactionWithDetails,
    settings: BusinessSettings,
    isPrinting: Boolean = false,
    printMessage: String? = null,
    printErrorMessage: String? = null,
    onPrintReceipt: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val trx = transactionWithDetails.transaction
    val items = transactionWithDetails.items
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(trx.createdAt))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                Box(
                    modifier = Modifier.size(56.dp).background(SuccessGreenContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Pembayaran berhasil",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900
                )
                Text(
                    CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol),
                    style = PosTextStyles.displayMoney,
                    color = SuccessGreen
                )

                Spacer(Modifier.height(16.dp))

                // Pratinjau struk
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            settings.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                        if (settings.phone.isNotBlank()) {
                            Text(settings.phone, style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }

                        ReceiptDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                trx.receiptNumber,
                                style = PosTextStyles.receiptNo,
                                color = Slate700
                            )
                            Text(
                                trx.paymentMethod.label(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate700
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(
                                "Kasir: ${trx.cashierId ?: "Admin"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }

                        ReceiptDivider()

                        items.forEach { itemDetail ->
                            val item = itemDetail.item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.productName} x${item.qty.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate900,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    CurrencyFormatter.format(item.subtotal, settings.currencySymbol),
                                    style = PosTextStyles.money,
                                    color = Slate900
                                )
                            }
                        }

                        ReceiptDivider()

                        ReceiptLine("Subtotal", CurrencyFormatter.format(trx.subtotal, settings.currencySymbol))

                        if (trx.discount > 0) {
                            ReceiptLine(
                                "Diskon",
                                "- ${CurrencyFormatter.format(trx.discount, settings.currencySymbol)}"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.bodyMedium, color = Slate900)
                            Text(
                                CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol),
                                style = PosTextStyles.money,
                                color = Slate900
                            )
                        }

                        ReceiptLine("Bayar", CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Kembalian",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            Text(
                                CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol),
                                style = PosTextStyles.money,
                                color = SuccessGreen
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            settings.footerNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (printMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    FeedbackBanner(message = printMessage, isError = false)
                }
                if (printErrorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    FeedbackBanner(message = printErrorMessage, isError = true)
                }

                Spacer(Modifier.height(16.dp))

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
                        enabled = !isPrinting
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
                            Text("Cetak struk", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Transaksi baru", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
    }
}

/** Garis pemisah bergaya perforasi struk (motif identitas). */
@Composable
private fun ReceiptDivider() {
    HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(value, style = PosTextStyles.money, color = Slate700)
    }
}

@Composable
private fun FeedbackBanner(message: String, isError: Boolean) {
    val container = if (isError) MaterialTheme.colorScheme.errorContainer else SuccessGreenContainer
    val tint = if (isError) DangerRed else SuccessGreen
    Surface(color = container, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun com.rising.pos.core.model.PaymentMethod.label(): String = when (this) {
    com.rising.pos.core.model.PaymentMethod.CASH -> "Tunai"
    com.rising.pos.core.model.PaymentMethod.QRIS -> "QRIS"
    com.rising.pos.core.model.PaymentMethod.BANK_TRANSFER -> "Transfer"
    com.rising.pos.core.model.PaymentMethod.DEBIT_CARD -> "Debit"
    else -> name
}
