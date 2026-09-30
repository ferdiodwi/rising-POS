package com.rising.pos.feature.pos.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.core.util.ReceiptFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transaksi Berhasil!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Thermal Receipt Paper
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Slate200.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = settings.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        if (settings.phone.isNotBlank()) {
                            Text(
                                text = settings.phone,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }

                        HorizontalDivider(
                            color = Slate200,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Meta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("No: ${trx.receiptNumber}", style = MaterialTheme.typography.labelSmall.copy(color = Slate700, fontFamily = FontFamily.Monospace))
                            Text(trx.paymentMethod.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate700))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formattedDate, style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                            Text("Kasir: ${trx.cashierId ?: "Admin"}", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        }

                        HorizontalDivider(
                            color = Slate200,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Items
                        items.forEach { itemDetail ->
                            val item = itemDetail.item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.productName} x${item.qty.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate900),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(item.subtotal, settings.currencySymbol),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Slate900)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Slate200,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Subtotal & Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                            Text(CurrencyFormatter.format(trx.subtotal, settings.currencySymbol), style = MaterialTheme.typography.bodySmall.copy(color = Slate700))
                        }

                        if (trx.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Diskon", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                                Text("- ${CurrencyFormatter.format(trx.discount, settings.currencySymbol)}", style = MaterialTheme.typography.bodySmall.copy(color = Slate700))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                            Text(CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bayar", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                            Text(CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol), style = MaterialTheme.typography.bodySmall.copy(color = Slate700))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kembalian", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                            Text(CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = settings.footerNote,
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Printing Feedback Banners
                if (printMessage != null) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = printMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                if (printErrorMessage != null) {
                    Surface(
                        color = DangerRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = printErrorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row (Bagikan & Cetak Struk)
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
                        enabled = !isPrinting
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
                            Text("Cetak Struk", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Transaction Baru Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text(
                        "Transaksi Baru",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
