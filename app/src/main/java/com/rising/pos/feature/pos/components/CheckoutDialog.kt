package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.BusinessType
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.components.*
import com.rising.pos.ui.theme.*

/** Reference payment layout with shared order totals and checkout callback. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutDialog(cart: CartState, settings: BusinessSettings, isProcessing: Boolean, errorMessage: String?,
    onDismiss: () -> Unit, onConfirmPayment: (PaymentMethod, Long, OrderType, String?) -> Unit
) = CashierTheme {
    val calc = cart.calculateTotals(settings.isTaxEnabled, settings.taxPercentage, settings.isTaxInclusive,
        settings.isServiceChargeEnabled, settings.serviceChargePercentage)
    var selectedOrderType by rememberSaveable { mutableStateOf(if (cart.orderType != OrderType.RETAIL) cart.orderType else if (settings.type == BusinessType.CAFE) OrderType.DINE_IN else cart.orderType) }
    var tableNumber by rememberSaveable { mutableStateOf(cart.tableNumber.orEmpty()) }
    var orderNote by rememberSaveable { mutableStateOf(cart.note.orEmpty()) }
    var selectedMethod by rememberSaveable { mutableStateOf(PaymentMethod.CASH) }
    var cashInput by rememberSaveable { mutableStateOf(calc.grandTotal.toString()) }
    var replaceCash by rememberSaveable { mutableStateOf(true) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    var isSplitPayment by rememberSaveable { mutableStateOf(false) }
    var splitMethod1 by rememberSaveable { mutableStateOf(PaymentMethod.CASH) }
    var splitMethod2 by rememberSaveable { mutableStateOf(PaymentMethod.QRIS) }
    var splitAmount1Input by rememberSaveable { mutableStateOf((calc.grandTotal / 2).toString()) }
    val splitAmount1 = splitAmount1Input.toLongOrNull() ?: 0L
    val splitAmount2 = (calc.grandTotal - splitAmount1).coerceAtLeast(0L)
    val splitValid = splitAmount1 > 0 && splitAmount1 < calc.grandTotal
    val cashAmount = cashInput.toLongOrNull() ?: 0L
    val canSubmit = cart.items.isNotEmpty() && !isProcessing && if (isSplitPayment) splitValid else selectedMethod != PaymentMethod.CASH || cashAmount >= calc.grandTotal
    val colors = MaterialTheme.colorScheme
    fun money(amount: Long) = posMoney(amount, settings.currencySymbol)

    PosDialog(onDismiss = { if (!isProcessing) onDismiss() }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss, enabled = !isProcessing, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali ke kasir", Modifier.size(26.dp))
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text("Pembayaran", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(settings.name, fontSize = 13.sp, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text("Total tagihan", color = colors.onSurfaceVariant, fontSize = 15.sp, lineHeight = 19.sp)
            Text(money(calc.grandTotal), fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${quantityLabel(cart.totalItemCount)} barang · ${cart.items.size} jenis produk", fontSize = 13.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                TextButton(onClick = { showDetails = !showDetails }, enabled = !isProcessing, modifier = Modifier.height(40.dp), contentPadding = PaddingValues(start = 4.dp)) {
                    Text(if (showDetails) "Tutup rincian" else "Lihat rincian", fontSize = 13.sp)
                    Icon(if (showDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp))
                }
            }
            if (showDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    cart.items.forEach { item -> Row {
                        Text("${quantityLabel(item.quantity)} × ${item.product.name}", Modifier.weight(1f), fontSize = 13.sp)
                        Text(money(item.totalPrice), fontSize = 13.sp)
                    } }
                    if (calc.discount > 0) Text("Diskon −${money(calc.discount)}", fontSize = 13.sp)
                    if (calc.tax > 0) Text("Pajak ${money(calc.tax)}${if (settings.isTaxInclusive) " (termasuk)" else ""}", fontSize = 13.sp)
                    if (calc.serviceCharge > 0) Text("Biaya layanan ${money(calc.serviceCharge)}", fontSize = 13.sp)
                    cart.customer?.let { Text("Pelanggan: ${it.name}", fontSize = 13.sp) }
                    if (settings.type == BusinessType.CAFE || settings.isTableEnabled) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(OrderType.DINE_IN to "Makan di tempat", OrderType.TAKEAWAY to "Bungkus", OrderType.DELIVERY to "Diantar").forEach { (type, label) ->
                                OptionChip(label, selectedOrderType == type, enabled = !isProcessing) { selectedOrderType = type }
                            }
                        }
                        if (settings.isTableEnabled && selectedOrderType == OrderType.DINE_IN) OutlinedTextField(tableNumber, { tableNumber = it }, label = { Text("Nomor meja") }, enabled = !isProcessing, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(orderNote, { orderNote = it }, label = { Text("Catatan pesanan (opsional)") }, enabled = !isProcessing, modifier = Modifier.fillMaxWidth())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selectedMethod == PaymentMethod.DEBIT_CARD && !isSplitPayment,
                            { selectedMethod = PaymentMethod.DEBIT_CARD; isSplitPayment = false }, enabled = !isProcessing, label = { Text("Kartu debit") })
                        FilterChip(isSplitPayment, { isSplitPayment = !isSplitPayment }, enabled = !isProcessing, label = { Text("Bagi pembayaran") })
                    }
                }
            }
            HorizontalDivider(); Spacer(Modifier.height(12.dp))
            Text("Metode pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple(PaymentMethod.CASH, "Tunai", CashierIcons.Money),
                    Triple(PaymentMethod.QRIS, "QRIS", Icons.Outlined.QrCode2),
                    Triple(PaymentMethod.BANK_TRANSFER, "Transfer", CashierIcons.Bank)).forEach { (method, label, icon) ->
                    PaymentTile(label, icon, selectedMethod == method && !isSplitPayment, !isProcessing,
                        { selectedMethod = method; isSplitPayment = false }, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))
            if (isSplitPayment) {
                SplitPaymentSection(settings, calc.grandTotal, splitMethod1, { if (!isProcessing) splitMethod1 = it },
                    splitAmount1Input, { if (!isProcessing) splitAmount1Input = it }, splitMethod2, { if (!isProcessing) splitMethod2 = it }, splitAmount2)
            } else if (selectedMethod == PaymentMethod.CASH) {
                Text("Uang diterima", fontSize = 14.sp, lineHeight = 18.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Surface(Modifier.fillMaxWidth().testTag("cashAmount"), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.2.dp, colors.primary)) {
                    Row(Modifier.padding(4.dp).heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(5.dp), color = colors.primaryContainer) {
                            Text(settings.currencySymbol, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurfaceVariant)
                        }
                        Text(groupedAmount(cashAmount), Modifier.padding(start = 12.dp), fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(calc.grandTotal to "Uang pas", 50_000L to money(50_000), 100_000L to money(100_000)).forEach { (value, label) ->
                        val selected = cashAmount == value
                        OutlinedButton({ cashInput = value.toString(); replaceCash = true }, enabled = !isProcessing,
                            modifier = Modifier.weight(1f).heightIn(min = 42.dp), shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) colors.primaryContainer else colors.surface,
                                contentColor = if (selected) colors.primary else colors.onSurface), contentPadding = PaddingValues(horizontal = 2.dp)) {
                            Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                val enough = cashAmount >= calc.grandTotal
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), color = if (enough) colors.primaryContainer else colors.errorContainer) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (enough) "Kembalian" else "Uang kurang", Modifier.weight(1f), fontSize = 15.sp, color = colors.onSurfaceVariant)
                        Text(money(if (enough) cashAmount - calc.grandTotal else calc.grandTotal - cashAmount), fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = if (enough) colors.primary else colors.error)
                    }
                }
                Spacer(Modifier.height(8.dp))
                CashKeypad(enabled = !isProcessing, onKey = { key -> cashInput = cashKeyInput(cashInput, key, replaceCash); replaceCash = false })
            } else {
                Text(if (selectedMethod == PaymentMethod.QRIS) "Pastikan pembayaran QRIS sudah berhasil diterima." else if (selectedMethod == PaymentMethod.DEBIT_CARD) "Kartu debit dipilih. Pastikan transaksi EDC berhasil." else "Pastikan transfer sudah masuk ke rekening toko.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                if (selectedMethod == PaymentMethod.DEBIT_CARD) TextButton({ showDetails = true }) { Text("Ubah metode lainnya") }
            }
            errorMessage?.let { Text(it, color = colors.error, modifier = Modifier.padding(vertical = 12.dp), fontSize = 14.sp) }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(if (!isSplitPayment && selectedMethod == PaymentMethod.CASH) "Pastikan uang tunai sudah diterima." else "Pastikan seluruh pembayaran sudah diterima.", fontSize = 11.sp, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Button(onClick = {
            val paid = if (!isSplitPayment && selectedMethod == PaymentMethod.CASH) cashAmount else calc.grandTotal
            val note = buildString {
                if (tableNumber.isNotBlank() && selectedOrderType == OrderType.DINE_IN) append("Meja: ${tableNumber.trim()}")
                if (isSplitPayment) {
                    if (isNotEmpty()) append(" • ")
                    append("Split: ${splitMethod1.label()} ${money(splitAmount1)} + ${splitMethod2.label()} ${money(splitAmount2)}")
                }
                if (orderNote.isNotBlank()) { if (isNotEmpty()) append(" • "); append(orderNote.trim()) }
            }.ifBlank { null }
            onConfirmPayment(if (isSplitPayment) splitMethod1 else selectedMethod, paid, selectedOrderType, note)
        }, enabled = canSubmit, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(6.dp)) {
            if (isProcessing) CircularProgressIndicator(Modifier.size(20.dp), color = colors.onPrimary, strokeWidth = 2.dp)
            else { Text("Selesaikan pembayaran", fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.width(10.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(22.dp)) }
        }
    }
}

/** Presets replace the next entry. Ignore oversized input rather than overflowing money. */
internal fun cashKeyInput(current: String, key: String, replace: Boolean): String {
    if (key == "delete") return current.dropLast(1).ifBlank { "0" }
    if (key !in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "000")) return current
    val next = ((if (replace) "" else current) + key).trimStart('0').ifBlank { "0" }
    return if (next.length <= 12 && next.toLongOrNull() != null) next else current
}
@Composable
private fun PaymentTile(label: String, icon: ImageVector, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(6.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 1.2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)) {
        Box(Modifier.heightIn(min = 76.dp)) {
            Column(Modifier.align(Alignment.Center).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, Modifier.size(31.dp), tint = if (selected) colors.primary else colors.onSurface)
                Spacer(Modifier.height(6.dp)); Text(label, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) colors.primary else colors.onSurface)
            }
            if (selected) Icon(Icons.Default.CheckCircle, "Terpilih", Modifier.align(Alignment.TopEnd).padding(6.dp).size(18.dp), tint = colors.primary)
        }
    }
}
@Composable
private fun CashKeypad(enabled: Boolean, onKey: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("000", "0", "delete")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    OutlinedButton(onClick = { onKey(key) }, enabled = enabled, modifier = Modifier.weight(1f).heightIn(min = 44.dp).testTag("key_$key"),
                        shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), contentPadding = PaddingValues(4.dp)) {
                        if (key == "delete") Icon(Icons.AutoMirrored.Outlined.Backspace, "Hapus digit", Modifier.size(27.dp))
                        else Text(key, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SplitPaymentSection(
    settings: BusinessSettings,
    grandTotal: Long,
    splitMethod1: PaymentMethod,
    onSplitMethod1: (PaymentMethod) -> Unit,
    splitAmount1Input: String,
    onSplitAmount1Input: (String) -> Unit,
    splitMethod2: PaymentMethod,
    onSplitMethod2: (PaymentMethod) -> Unit,
    splitAmount2: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("1. Pembayaran pertama", style = MaterialTheme.typography.labelLarge, color = Slate900)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PaymentMethod.CASH to "Tunai",
                    PaymentMethod.QRIS to "QRIS",
                    PaymentMethod.BANK_TRANSFER to "Transfer",
                    PaymentMethod.DEBIT_CARD to "Debit"
                ).forEach { (method, label) ->
                    OptionChip(
                        label = label,
                        selected = splitMethod1 == method,
                        onClick = { onSplitMethod1(method) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = splitAmount1Input,
                    onValueChange = { onSplitAmount1Input(it.filter { ch -> ch.isDigit() }) },
                    prefix = { Text("${settings.currencySymbol} ") },
                    label = { Text("Nominal bagian 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedButton(
                    onClick = { onSplitAmount1Input((grandTotal / 2).toString()) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("50:50", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }

            HorizontalDivider(color = Slate200)

            Text("2. Pembayaran kedua (sisa)", style = MaterialTheme.typography.labelLarge, color = Slate900)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PaymentMethod.QRIS to "QRIS",
                    PaymentMethod.BANK_TRANSFER to "Transfer",
                    PaymentMethod.DEBIT_CARD to "Debit",
                    PaymentMethod.CASH to "Tunai"
                ).forEach { (method, label) ->
                    OptionChip(
                        label = label,
                        selected = splitMethod2 == method,
                        onClick = { onSplitMethod2(method) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sisa tagihan bagian 2", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                Text(
                    CurrencyFormatter.format(splitAmount2, settings.currencySymbol),
                    style = PosTextStyles.money,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(999.dp),
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

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer"
    PaymentMethod.DEBIT_CARD -> "Debit"
    else -> name
}
