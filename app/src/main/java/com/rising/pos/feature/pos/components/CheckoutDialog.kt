package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.BusinessType
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer
import com.rising.pos.ui.theme.WarningAmber
import com.rising.pos.ui.theme.WarningAmberContainer

/**
 * Dialog pembayaran.
 *
 * Alur uang dijaga sederhana dan aman: total besar di atas, pilih metode, lalu
 * untuk tunai tampil input + kembalian yang langsung berubah. Kekurangan uang
 * ditandai dengan IKON + TEKS, bukan hanya warna.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutDialog(
    cart: CartState,
    settings: BusinessSettings,
    isProcessing: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmPayment: (PaymentMethod, Long, OrderType, String?) -> Unit
) {
    val calc = cart.calculateTotals(
        isTaxEnabled = settings.isTaxEnabled,
        taxPercentage = settings.taxPercentage,
        isTaxInclusive = settings.isTaxInclusive,
        isServiceChargeEnabled = settings.isServiceChargeEnabled,
        serviceChargePercentage = settings.serviceChargePercentage
    )

    var selectedOrderType by remember {
        mutableStateOf(
            if (cart.orderType != OrderType.RETAIL) cart.orderType
            else if (settings.type == BusinessType.CAFE) OrderType.DINE_IN
            else cart.orderType
        )
    }
    var tableNumber by remember { mutableStateOf(cart.tableNumber ?: "") }
    var orderNote by remember { mutableStateOf(cart.note ?: "") }

    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var cashInput by remember { mutableStateOf(calc.grandTotal.toString()) }

    var isSplitPayment by remember { mutableStateOf(false) }
    var splitMethod1 by remember { mutableStateOf(PaymentMethod.CASH) }
    var splitAmount1Input by remember { mutableStateOf((calc.grandTotal / 2).toString()) }
    var splitMethod2 by remember { mutableStateOf(PaymentMethod.QRIS) }

    val splitAmount1 = splitAmount1Input.toLongOrNull() ?: 0L
    val splitAmount2 = (calc.grandTotal - splitAmount1).coerceAtLeast(0L)
    val isSplitValid = splitAmount1 > 0L && splitAmount2 > 0L && (splitAmount1 + splitAmount2 == calc.grandTotal)

    val cashAmount = cashInput.toLongOrNull() ?: 0L
    val changeAmount = (cashAmount - calc.grandTotal).coerceAtLeast(0L)
    val shortfall = (calc.grandTotal - cashAmount).coerceAtLeast(0L)
    val isCashSufficient = cashAmount >= calc.grandTotal

    val canSubmit = !isProcessing && if (isSplitPayment) {
        isSplitValid
    } else {
        selectedMethod != PaymentMethod.CASH || isCashSufficient
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .imePadding()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    // Judul
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pembayaran", style = MaterialTheme.typography.titleLarge, color = Slate900)
                        Text(
                            "${cart.totalItemCount.toInt()} item",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Total: satu angka paling menonjol di layar.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Total pembayaran",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                                style = PosTextStyles.displayMoney,
                                color = Slate900
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Tipe pesanan (kafe / meja)
                    if (settings.type == BusinessType.CAFE || settings.isTableEnabled) {
                        Text("Tipe pesanan", style = MaterialTheme.typography.labelLarge, color = Slate900)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                OrderType.DINE_IN to "Makan di tempat",
                                OrderType.TAKEAWAY to "Bungkus",
                                OrderType.DELIVERY to "Diantar"
                            ).forEach { (type, label) ->
                                OptionChip(
                                    label = label,
                                    selected = selectedOrderType == type,
                                    onClick = { selectedOrderType = type }
                                )
                            }
                        }

                        if (settings.isTableEnabled && selectedOrderType == OrderType.DINE_IN) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = tableNumber,
                                onValueChange = { tableNumber = it },
                                label = { Text("Nomor meja") },
                                placeholder = { Text("Contoh: Meja 03") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Pelanggan
                    if (cart.customer != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        cart.customer.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (!cart.customer.phone.isNullOrBlank()) {
                                        Text(
                                            cart.customer.phone,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = orderNote,
                        onValueChange = { orderNote = it },
                        label = { Text("Catatan pesanan (opsional)") },
                        placeholder = { Text("Contoh: Less ice, jangan pedas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Mode pembayaran
                    Text("Metode pembayaran", style = MaterialTheme.typography.labelLarge, color = Slate900)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionChip(
                            label = "Penuh",
                            selected = !isSplitPayment,
                            onClick = { isSplitPayment = false }
                        )
                        OptionChip(
                            label = "Bagi pembayaran",
                            selected = isSplitPayment,
                            onClick = { isSplitPayment = true }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    if (isSplitPayment) {
                        SplitPaymentSection(
                            settings = settings,
                            grandTotal = calc.grandTotal,
                            splitMethod1 = splitMethod1,
                            onSplitMethod1 = { splitMethod1 = it },
                            splitAmount1Input = splitAmount1Input,
                            onSplitAmount1Input = { splitAmount1Input = it },
                            splitMethod2 = splitMethod2,
                            onSplitMethod2 = { splitMethod2 = it },
                            splitAmount2 = splitAmount2
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                PaymentMethod.CASH to "Tunai",
                                PaymentMethod.QRIS to "QRIS",
                                PaymentMethod.BANK_TRANSFER to "Transfer bank",
                                PaymentMethod.DEBIT_CARD to "Kartu debit"
                            ).forEach { (method, label) ->
                                MethodChip(
                                    label = label,
                                    selected = selectedMethod == method,
                                    icon = when (method) {
                                        PaymentMethod.CASH -> Icons.Default.Money
                                        PaymentMethod.QRIS -> Icons.Default.QrCode
                                        PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                                        else -> Icons.Default.CreditCard
                                    },
                                    onClick = { selectedMethod = method }
                                )
                            }
                        }

                        if (selectedMethod == PaymentMethod.CASH) {
                            Spacer(Modifier.height(16.dp))
                            CashSection(
                                currencySymbol = settings.currencySymbol,
                                grandTotal = calc.grandTotal,
                                cashInput = cashInput,
                                onCashInput = { cashInput = it },
                                isCashSufficient = isCashSufficient,
                                changeAmount = changeAmount,
                                shortfall = shortfall
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DangerRed
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                HorizontalDivider(color = Slate200)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    }

                    Button(
                        onClick = {
                            val paid = if (isSplitPayment) {
                                calc.grandTotal
                            } else if (selectedMethod == PaymentMethod.CASH) {
                                cashAmount
                            } else {
                                calc.grandTotal
                            }

                            val combinedNote = buildString {
                                if (tableNumber.isNotBlank() && selectedOrderType == OrderType.DINE_IN) {
                                    append("Meja: ${tableNumber.trim()}")
                                }
                                if (isSplitPayment) {
                                    if (isNotEmpty()) append(" • ")
                                    append(
                                        "Split: ${splitMethod1.label()} " +
                                            "${CurrencyFormatter.format(splitAmount1, settings.currencySymbol)} + " +
                                            "${splitMethod2.label()} " +
                                            "${CurrencyFormatter.format(splitAmount2, settings.currencySymbol)}"
                                    )
                                }
                                if (orderNote.isNotBlank()) {
                                    if (isNotEmpty()) append(" • ")
                                    append(orderNote.trim())
                                }
                            }.ifBlank { null }

                            val finalMethod = if (isSplitPayment) splitMethod1 else selectedMethod
                            onConfirmPayment(finalMethod, paid, selectedOrderType, combinedNote)
                        },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Selesaikan bayar",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashSection(
    currencySymbol: String,
    grandTotal: Long,
    cashInput: String,
    onCashInput: (String) -> Unit,
    isCashSufficient: Boolean,
    changeAmount: Long,
    shortfall: Long
) {
    Text("Uang diterima", style = MaterialTheme.typography.labelLarge, color = Slate900)
    Spacer(Modifier.height(8.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        QuickCashButton(label = "Uang pas", onClick = { onCashInput(grandTotal.toString()) })
        listOf(10_000L, 20_000L, 50_000L, 100_000L).forEach { add ->
            QuickCashButton(
                label = "+${add / 1000}k",
                onClick = {
                    val current = cashInput.toLongOrNull() ?: 0L
                    onCashInput((current + add).toString())
                }
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    OutlinedTextField(
        value = cashInput,
        onValueChange = { onCashInput(it.filter { ch -> ch.isDigit() }) },
        prefix = { Text("$currencySymbol ") },
        label = { Text("Nominal uang tunai") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = !isCashSufficient,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(10.dp))

    // Kembalian / kekurangan: ikon + teks, bukan warna saja.
    val isEnough = isCashSufficient
    Surface(
        color = if (isEnough) SuccessGreenContainer else WarningAmberContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEnough) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = if (isEnough) SuccessGreen else WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEnough) "Kembalian" else "Uang kurang",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            }
            Text(
                text = CurrencyFormatter.format(
                    if (isEnough) changeAmount else shortfall,
                    currencySymbol
                ),
                style = PosTextStyles.money,
                color = if (isEnough) SuccessGreen else WarningAmber
            )
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
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
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

@Composable
private fun MethodChip(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Slate200
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = Slate500,
            iconColor = Slate500,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun QuickCashButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate200),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Slate700)
    }
}

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Tunai"
    PaymentMethod.QRIS -> "QRIS"
    PaymentMethod.BANK_TRANSFER -> "Transfer"
    PaymentMethod.DEBIT_CARD -> "Debit"
    else -> name
}
