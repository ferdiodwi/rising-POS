package com.rising.pos.feature.transaction

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.model.*
import com.rising.pos.ui.components.*
import com.rising.pos.ui.theme.*
import java.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TransactionTabletMasterDetail(
    transactions: List<TransactionWithDetails>, totalCompletedSales: Long, completedCount: Int,
    period: HistoryPeriod, customDate: LocalDate?, customDateText: String, todayDateFormatted: String,
    currencySymbol: String, searchQuery: String, onSearchChange: (String) -> Unit,
    onSelectPeriod: (HistoryPeriod) -> Unit, onPickCustomDate: () -> Unit,
    onPrintReceipt: (TransactionWithDetails) -> Unit, onShareReceipt: (TransactionWithDetails) -> Unit,
    onManageTransaction: (TransactionWithDetails) -> Unit, isPrinting: Boolean,
    modifier: Modifier = Modifier
) = TabletCashierTheme {
    val colors = MaterialTheme.colorScheme
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var page by rememberSaveable(searchQuery, period, customDate) { mutableStateOf(0) }
    val pages = ((transactions.size + 5) / 6).coerceAtLeast(1)
    val safePage = page.coerceIn(0, pages - 1)
    val visible = transactions.drop(safePage * 6).take(6)
    val selected = visible.firstOrNull { it.transaction.id == selectedId } ?: visible.firstOrNull()
    val time = remember { SimpleDateFormat("HH.mm", Locale.forLanguageTag("id-ID")) }
    fun money(value: Long) = posMoney(value, currencySymbol)
    val periodTitle = if (customDate != null) customDateText else period.label
    Surface(modifier, color = colors.surface, contentColor = colors.onSurface) {
        TabletWorkspaceSplit(master = {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("history_master")) {
                PosSearchBar(searchQuery, onSearchChange, "Cari nomor struk")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PosDateFilterButton("Hari ini", period == HistoryPeriod.TODAY && customDate == null, { onSelectPeriod(HistoryPeriod.TODAY) })
                    PosDateFilterButton("7 hari", period == HistoryPeriod.LAST_7_DAYS && customDate == null, { onSelectPeriod(HistoryPeriod.LAST_7_DAYS) })
                    PosDateFilterButton(customDateText, customDate != null, onPickCustomDate, icon = Icons.Outlined.CalendarToday)
                    Text(if (customDate != null) customDateText else todayDateFormatted, Modifier.padding(12.dp), fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, colors.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Penjualan ${periodTitle.lowercase()}", fontSize = 13.sp, color = colors.onSurfaceVariant)
                            Text(money(totalCompletedSales), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        VerticalDivider(Modifier.height(64.dp), color = colors.outlineVariant)
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text("Transaksi selesai", fontSize = 13.sp, color = colors.onSurfaceVariant)
                            Text("$completedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Transaksi batal tidak dihitung.", fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    }
                }
                Text(periodTitle, Modifier.padding(vertical = 12.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val tableWidth = maxWidth.coerceAtLeast(540.dp)
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(tableWidth).border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))) {
                            Row(Modifier.fillMaxWidth().background(colors.surfaceContainer).padding(12.dp)) {
                                Text("No. struk", Modifier.weight(2.3f), fontSize = 13.sp)
                                Text("Waktu", Modifier.weight(0.8f), fontSize = 13.sp)
                                Text("Metode", Modifier.weight(1.1f), fontSize = 13.sp)
                                Text("Total", Modifier.weight(1.3f), fontSize = 13.sp)
                                Text("Status", Modifier.weight(1f), fontSize = 13.sp)
                            }
                            visible.forEach { item ->
                                val transaction = item.transaction
                                Surface(onClick = { selectedId = transaction.id }, color = if (selected?.transaction?.id == transaction.id) colors.primaryContainer else colors.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (selected?.transaction?.id == transaction.id) BorderStroke(1.dp, colors.primary) else null,
                                    modifier = Modifier.testTag("history_row_${transaction.id}")) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Row(Modifier.weight(2.3f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(CashierIcons.Receipt, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
                                            Spacer(Modifier.width(8.dp))
                                            Text(transaction.receiptNumber, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text(time.format(Date(transaction.createdAt)), Modifier.weight(0.8f), fontSize = 13.sp)
                                        Text(historyPaymentLabel(transaction.paymentMethod), Modifier.weight(1.1f), fontSize = 13.sp)
                                        Text(money(transaction.grandTotal), Modifier.weight(1.3f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Box(Modifier.weight(1f)) { HistoryStatus(transaction.status) }
                                    }
                                }
                                HorizontalDivider(color = colors.outlineVariant)
                            }
                        }
                    }
                }
                if (transactions.isEmpty()) WorkspaceEmptyState(CashierIcons.Receipt, "Belum ada transaksi", "Ubah pencarian atau tanggal, atau selesaikan penjualan di Kasir.")
            }
            WorkspacePagination(transactions.size, safePage, 6, "transaksi") { page = it }
        }, detail = {
            if (selected != null) {
                val transaction = selected.transaction
                key(transaction.id) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("history_detail")) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Detail transaksi", Modifier.weight(1f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        HistoryStatus(transaction.status)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(transaction.receiptNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis)
                    Text(SimpleDateFormat("d MMMM yyyy • HH.mm", Locale.forLanguageTag("id-ID")).format(Date(transaction.createdAt)), fontSize = 13.sp, color = colors.onSurfaceVariant)
                    HorizontalDivider(Modifier.padding(vertical = 14.dp), color = colors.outlineVariant)
                    selected.items.forEach { line ->
                        val item = line.item
                        WorkspaceDetailField(item.productName, money(item.subtotal))
                        Text("${quantityLabel(item.qty)} × ${money(item.unitPrice)}", fontSize = 13.sp, color = colors.onSurfaceVariant)
                        if (line.modifiers.isNotEmpty()) Text(line.modifiers.joinToString(", ") { it.modifierName }, fontSize = 12.sp, color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                    WorkspaceDetailField("Subtotal", money(transaction.subtotal))
                    WorkspaceDetailField("Diskon", money(transaction.discount))
                    if (transaction.serviceCharge > 0) WorkspaceDetailField("Biaya layanan", money(transaction.serviceCharge))
                    if (transaction.tax > 0) WorkspaceDetailField("Pajak", money(transaction.tax))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                    WorkspaceDetailField("Total", money(transaction.grandTotal), prominent = true)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                    WorkspaceDetailField("Metode pembayaran", historyPaymentLabel(transaction.paymentMethod))
                    transaction.splitPaymentMethod?.let { WorkspaceDetailField("Pembayaran kedua (${historyPaymentLabel(it)})", money(transaction.splitAmount)) }
                    WorkspaceDetailField("Uang diterima", money(transaction.paymentAmount))
                    WorkspaceDetailField("Kembalian", money(transaction.changeAmount))
                    TextButton(onClick = { onManageTransaction(selected) }) { Text("Tindakan transaksi") }
                }
                }
                WorkspaceAction(if (isPrinting) "Mencetak…" else "Cetak struk", Icons.Outlined.Print, { onPrintReceipt(selected) }, enabled = !isPrinting)
                Spacer(Modifier.height(8.dp))
                WorkspaceAction("Bagikan struk", Icons.Outlined.Share, { onShareReceipt(selected) }, secondary = true)
            } else WorkspaceEmptyState(CashierIcons.Receipt, "Detail transaksi", "Pilih transaksi untuk melihat struk dan rincian pembayaran.")
        })
    }
}

@Composable
private fun HistoryStatus(status: TransactionStatus) {
    val (label, color, background) = when (status) {
        TransactionStatus.COMPLETED -> Triple("Selesai", SuccessGreen, SuccessGreenContainer)
        TransactionStatus.CANCELLED -> Triple("Batal", DangerRed, DangerRedContainer)
        TransactionStatus.REFUNDED -> Triple("Refund", WarningAmber, WarningAmberContainer)
        TransactionStatus.HELD -> Triple("Ditahan", WarningAmber, WarningAmberContainer)
    }
    WorkspaceBadge(label, color, background)
}

private fun historyPaymentLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer"
    PaymentMethod.DEBIT_CARD -> "Debit"
    PaymentMethod.CREDIT_CARD -> "Kredit"
    PaymentMethod.E_WALLET -> "E-wallet"
    PaymentMethod.OTHER -> "Lainnya"
}
