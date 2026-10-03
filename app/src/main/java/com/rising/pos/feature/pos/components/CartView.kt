package com.rising.pos.feature.pos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.components.QuantityStepper
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.PosTextStyles

/** Wrap short orders on phones; keep the totals anchored in the tablet panel. */
@Composable
fun CartView(
    cart: CartState,
    settings: BusinessSettings,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onHoldCart: () -> Unit = {},
    onOpenCustomerPicker: () -> Unit = {},
    onRemoveCustomer: () -> Unit = {},
    onOpenDiscountDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
    onAddProducts: (() -> Unit)? = null,
    compact: Boolean = false
) {
    val calc = cart.calculateTotals(
        isTaxEnabled = settings.isTaxEnabled,
        taxPercentage = settings.taxPercentage,
        isTaxInclusive = settings.isTaxInclusive,
        isServiceChargeEnabled = settings.isServiceChargeEnabled,
        serviceChargePercentage = settings.serviceChargePercentage
    )
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    fun money(amount: Long) = CurrencyFormatter.format(amount, settings.currencySymbol)

    Column(
        modifier = modifier.then(if (compact) Modifier else Modifier.fillMaxHeight()).padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Keranjang", style = MaterialTheme.typography.titleLarge)
                Text("${cart.items.size} jenis barang", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            if (cart.items.isNotEmpty()) {
                TextButton(onClick = { confirmClear = true }) {
                    Text("Kosongkan", color = colors.error)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = !compact),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    onClick = onOpenCustomerPicker,
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(start = 12.dp, end = 4.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PersonAdd, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cart.customer?.name ?: "Tambah pelanggan", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (cart.customer == null) Text("Opsional", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        if (cart.customer != null) {
                            IconButton(onClick = onRemoveCustomer) { Icon(Icons.Default.Close, "Lepas pelanggan") }
                        } else {
                            Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
            if (cart.items.isEmpty()) {
                item {
                    WorkspaceEmptyState(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Keranjang masih kosong",
                        description = "Tambahkan produk untuk mulai membuat pesanan."
                    )
                }
            }
            items(cart.items, key = { it.cartItemId }) { item ->
                CartItemRow(item, settings.currencySymbol, onUpdateQuantity, onRemoveItem)
            }
            onAddProducts?.let { addProducts ->
                item {
                    OutlinedButton(
                        onClick = addProducts,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah barang")
                    }
                }
            }
            if (cart.items.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(color = colors.outlineVariant)
                        SummaryLine("Subtotal", money(calc.subtotal))
                        Surface(onClick = onOpenDiscountDialog, color = colors.surface) {
                            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (calc.discount > 0) "Diskon" else "Tambah diskon", color = colors.primary, modifier = Modifier.weight(1f))
                                if (calc.discount > 0) Text("− ${money(calc.discount)}", style = PosTextStyles.money)
                                Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                            }
                        }
                        if (!cart.discountReason.isNullOrBlank()) {
                            Text(cart.discountReason, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        if (calc.serviceCharge > 0) SummaryLine("Biaya layanan", money(calc.serviceCharge))
                        if (settings.isTaxEnabled && calc.tax > 0) {
                            SummaryLine(
                                if (settings.isTaxInclusive) "Pajak (sudah termasuk)" else "Pajak (${settings.taxPercentage}%)",
                                money(calc.tax)
                            )
                        }
                    }
                }
            }
        }
        if (cart.items.isNotEmpty()) {
            HorizontalDivider(color = colors.outlineVariant)
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Stack the label and value to keep large totals readable on narrow phones.
                Text("Total pembayaran", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(money(calc.grandTotal), style = PosTextStyles.displayMoney)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onHoldCart, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tahan")
                    }
                    Button(onClick = onCheckout, modifier = Modifier.weight(1.5f).heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("Bayar", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Kosongkan keranjang?") },
            text = { Text("Semua barang di pesanan ini akan dihapus dari keranjang.") },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClearCart() }) { Text("Kosongkan", color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = PosTextStyles.money)
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    currencySymbol: String,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                item.variant?.let { Text(it.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(
                    "${CurrencyFormatter.format(item.unitPrice, currencySymbol)} / ${item.product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onRemoveItem(item.cartItemId) }) {
                Icon(Icons.Default.DeleteOutline, "Hapus ${item.product.name}", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(CurrencyFormatter.format(item.totalPrice, currencySymbol), style = PosTextStyles.money, modifier = Modifier.weight(1f))
            QuantityStepper(
                quantity = item.quantity,
                productName = item.product.name,
                canDecrease = item.quantity > 1,
                onDecrease = { onUpdateQuantity(item.cartItemId, -1.0) },
                onIncrease = { onUpdateQuantity(item.cartItemId, 1.0) }
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
