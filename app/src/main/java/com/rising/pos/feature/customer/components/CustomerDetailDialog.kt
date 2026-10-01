package com.rising.pos.feature.customer.components

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
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.CustomerWithStats
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
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
 * Profil pelanggan: identitas, statistik belanja, kontak, dan riwayat transaksi.
 */
@Composable
fun CustomerDetailDialog(
    customerWithStats: CustomerWithStats,
    transactions: List<TransactionWithDetails>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cust = customerWithStats.customer
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Profil pelanggan", style = MaterialTheme.typography.titleMedium, color = Slate900)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cust.name.trim().take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = cust.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate900,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Statistik belanja
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = "Total belanja",
                        value = CurrencyFormatter.format(customerWithStats.totalSpent, currencySymbol),
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Transaksi",
                        value = "${customerWithStats.totalTransactions}x",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (customerWithStats.lastTransactionDate != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Kunjungan terakhir ${dateFormatter.format(Date(customerWithStats.lastTransactionDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Kontak
                val hasContact = !cust.phone.isNullOrBlank() || !cust.email.isNullOrBlank() ||
                    !cust.address.isNullOrBlank() || !cust.notes.isNullOrBlank()

                if (hasContact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!cust.phone.isNullOrBlank()) {
                            DetailItemRow(icon = Icons.Outlined.Phone, label = "Telepon", value = cust.phone)
                        }
                        if (!cust.email.isNullOrBlank()) {
                            DetailItemRow(icon = Icons.Outlined.Email, label = "Email", value = cust.email)
                        }
                        if (!cust.address.isNullOrBlank()) {
                            DetailItemRow(icon = Icons.Outlined.LocationOn, label = "Alamat", value = cust.address)
                        }
                        if (!cust.notes.isNullOrBlank()) {
                            DetailItemRow(icon = Icons.AutoMirrored.Outlined.Notes, label = "Catatan", value = cust.notes)
                        }
                    }
                } else {
                    Text(
                        text = "Belum ada detail kontak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(Modifier.height(12.dp))

                // Riwayat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Riwayat transaksi", style = MaterialTheme.typography.titleSmall, color = Slate900)
                    Text(
                        "${transactions.size} struk",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat transaksi untuk pelanggan ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transactions.take(5).forEach { item ->
                            val trx = item.transaction
                            val isCompleted = trx.status == TransactionStatus.COMPLETED
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = trx.receiptNumber,
                                            style = PosTextStyles.receiptNo,
                                            color = Slate700,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = dateFormatter.format(Date(trx.createdAt)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.format(trx.grandTotal, currencySymbol),
                                            style = PosTextStyles.money,
                                            color = if (isCompleted) Slate900 else DangerRed
                                        )
                                        StatusTag(status = trx.status)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hapus", style = MaterialTheme.typography.labelLarge, color = DangerRed)
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(value, style = PosTextStyles.priceCard, color = accent)
        }
    }
}

/** Status singkat: warna + ikon + teks. */
@Composable
private fun StatusTag(status: TransactionStatus) {
    val (container, content, label) = when (status) {
        TransactionStatus.COMPLETED -> Triple(SuccessGreenContainer, SuccessGreen, "Selesai")
        TransactionStatus.CANCELLED -> Triple(DangerRedContainer, DangerRed, "Void")
        TransactionStatus.REFUNDED -> Triple(DangerRedContainer, DangerRed, "Refund")
        TransactionStatus.HELD -> Triple(SuccessGreenContainer, SuccessGreen, "Ditahan")
    }
    Surface(shape = RoundedCornerShape(999.dp), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}

@Composable
private fun DetailItemRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Slate500,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Slate900)
        }
    }
}
