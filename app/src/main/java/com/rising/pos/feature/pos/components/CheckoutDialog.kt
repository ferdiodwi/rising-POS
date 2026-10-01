package com.rising.pos.feature.pos.components

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.BusinessType
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutDialog(
    cart: CartState,
    settings: BusinessSettings,
    isProcessing: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmPayment: (PaymentMethod, Double, OrderType, String?) -> Unit
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
    var cashInput by remember { mutableStateOf(calc.grandTotal.toInt().toString()) }

    var isSplitPayment by remember { mutableStateOf(false) }
    var splitMethod1 by remember { mutableStateOf(PaymentMethod.CASH) }
    var splitAmount1Input by remember { mutableStateOf((calc.grandTotal / 2).toInt().toString()) }
    var splitMethod2 by remember { mutableStateOf(PaymentMethod.QRIS) }

    val splitAmount1 = splitAmount1Input.toDoubleOrNull() ?: 0.0
    val splitAmount2 = maxOf(0.0, calc.grandTotal - splitAmount1)
    val isSplitValid = splitAmount1 > 0 && splitAmount2 > 0 && (splitAmount1 + splitAmount2 == calc.grandTotal)

    val cashAmount = cashInput.toDoubleOrNull() ?: 0.0
    val changeAmount = maxOf(0.0, cashAmount - calc.grandTotal)
    val isCashSufficient = cashAmount >= calc.grandTotal

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .imePadding()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                ) {
                    // Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pembayaran",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "${cart.totalItemCount.toInt()} Item",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 12.dp))

                    // Grand Total Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryBlue.copy(alpha = 0.08f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total pembayaran",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Order Type Selector (if Cafe or Table Enabled)
                    if (settings.type == BusinessType.CAFE || settings.isTableEnabled) {
                        Text(
                            text = "Tipe Pesanan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                OrderType.DINE_IN to "Makan di tempat",
                                OrderType.TAKEAWAY to "Bungkus",
                                OrderType.DELIVERY to "Diantar"
                            ).forEach { (type, label) ->
                                FilterChip(
                                    selected = selectedOrderType == type,
                                    onClick = { selectedOrderType = type },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        if (settings.isTableEnabled && selectedOrderType == OrderType.DINE_IN) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tableNumber,
                                onValueChange = { tableNumber = it },
                                label = { Text("Nomor Meja", fontSize = 12.sp) },
                                placeholder = { Text("Contoh: Meja 03", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Customer Display
                    if (cart.customer != null) {
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pelanggan: ${cart.customer.name}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    if (!cart.customer.phone.isNullOrBlank()) {
                                        Text(
                                            text = cart.customer.phone,
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Customer / Order Note Field
                    OutlinedTextField(
                        value = orderNote,
                        onValueChange = { orderNote = it },
                        label = { Text("Catatan Pesanan (Opsional)", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Less ice, jangan pedas", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Method Selector & Split Toggle
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Metode Pembayaran",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !isSplitPayment,
                                onClick = { isSplitPayment = false },
                                label = { Text("Penuh", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = isSplitPayment,
                                onClick = { isSplitPayment = true },
                                label = { Text("Bagi pembayaran", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSplitPayment) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "1. Pembayaran Pertama",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        PaymentMethod.CASH to "Tunai",
                                        PaymentMethod.QRIS to "QRIS",
                                        PaymentMethod.BANK_TRANSFER to "Transfer",
                                        PaymentMethod.DEBIT_CARD to "Debit"
                                    ).forEach { (method, label) ->
                                        FilterChip(
                                            selected = splitMethod1 == method,
                                            onClick = { splitMethod1 = method },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = splitAmount1Input,
                                        onValueChange = { splitAmount1Input = it.filter { ch -> ch.isDigit() } },
                                        prefix = { Text("${settings.currencySymbol} ", fontSize = 12.sp) },
                                        label = { Text("Nominal Bagian 1", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            splitAmount1Input = (calc.grandTotal / 2).toInt().toString()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("50:50", fontSize = 11.sp)
                                    }
                                }

                                HorizontalDivider(color = Slate200)

                                Text(
                                    text = "2. Pembayaran Kedua (Sisa)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        PaymentMethod.QRIS to "QRIS",
                                        PaymentMethod.BANK_TRANSFER to "Transfer",
                                        PaymentMethod.DEBIT_CARD to "Debit",
                                        PaymentMethod.CASH to "Tunai"
                                    ).forEach { (method, label) ->
                                        FilterChip(
                                            selected = splitMethod2 == method,
                                            onClick = { splitMethod2 = method },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sisa Tagihan Bagian 2:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(splitAmount2, settings.currencySymbol),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                PaymentMethod.CASH to "Tunai",
                                PaymentMethod.QRIS to "QRIS",
                                PaymentMethod.BANK_TRANSFER to "Transfer Bank",
                                PaymentMethod.DEBIT_CARD to "Kartu Debit"
                            ).forEach { (method, label) ->
                                val isSelected = selectedMethod == method
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMethod = method },
                                    label = { Text(label) },
                                    leadingIcon = {
                                        val icon = when (method) {
                                            PaymentMethod.CASH -> Icons.Default.Money
                                            PaymentMethod.QRIS -> Icons.Default.QrCode
                                            PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                                            else -> Icons.Default.CreditCard
                                        }
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // If CASH: Show Quick Cash Buttons and Tendered Amount Input
                        if (selectedMethod == PaymentMethod.CASH) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Uang Diterima",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Cash Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { cashInput = calc.grandTotal.toInt().toString() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = ButtonDefaults.ContentPadding
                                ) {
                                    Text("Uang Pas", fontSize = 12.sp)
                                }

                                listOf(10_000, 20_000, 50_000, 100_000).forEach { addAmount ->
                                    OutlinedButton(
                                        onClick = {
                                            val current = cashInput.toDoubleOrNull() ?: 0.0
                                            cashInput = (current + addAmount).toInt().toString()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+${addAmount / 1000}k", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cashInput,
                                onValueChange = { cashInput = it.filter { ch -> ch.isDigit() } },
                                prefix = { Text("${settings.currencySymbol} ") },
                                label = { Text("Nominal Uang Tunai") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = !isCashSufficient,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Change display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kembalian:",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                                )
                                Text(
                                    text = CurrencyFormatter.format(changeAmount, settings.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCashSufficient) SuccessGreen else DangerRed
                                    )
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(color = DangerRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
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
                                    val m1 = when (splitMethod1) {
                                        PaymentMethod.CASH -> "Tunai"
                                        PaymentMethod.QRIS -> "QRIS"
                                        PaymentMethod.BANK_TRANSFER -> "Transfer"
                                        else -> "Debit"
                                    }
                                    val m2 = when (splitMethod2) {
                                        PaymentMethod.CASH -> "Tunai"
                                        PaymentMethod.QRIS -> "QRIS"
                                        PaymentMethod.BANK_TRANSFER -> "Transfer"
                                        else -> "Debit"
                                    }
                                    append("Split: $m1 ${CurrencyFormatter.format(splitAmount1, settings.currencySymbol)} + $m2 ${CurrencyFormatter.format(splitAmount2, settings.currencySymbol)}")
                                }
                                if (orderNote.isNotBlank()) {
                                    if (isNotEmpty()) append(" • ")
                                    append(orderNote.trim())
                                }
                            }.ifBlank { null }

                            val finalMethod = if (isSplitPayment) splitMethod1 else selectedMethod
                            onConfirmPayment(finalMethod, paid, selectedOrderType, combinedNote)
                        },
                        enabled = !isProcessing && if (isSplitPayment) isSplitValid else (selectedMethod != PaymentMethod.CASH || isCashSufficient),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
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
    }
}
