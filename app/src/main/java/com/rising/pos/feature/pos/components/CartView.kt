package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.components.*
import com.rising.pos.ui.theme.CashierTheme
import com.rising.pos.ui.theme.posMoney

@Composable
fun CartView(cart: CartState, settings: BusinessSettings,
    onUpdateQuantity: (String, Double) -> Unit, onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit, onCheckout: () -> Unit, onHoldCart: () -> Unit = {},
    onOpenCustomerPicker: () -> Unit = {}, onRemoveCustomer: () -> Unit = {},
    onOpenDiscountDialog: () -> Unit = {}, modifier: Modifier = Modifier,
    onAddProducts: (() -> Unit)? = null, compact: Boolean = false
) = CashierTheme {
    val calc = cart.calculateTotals(settings.isTaxEnabled, settings.taxPercentage, settings.isTaxInclusive,
        settings.isServiceChargeEnabled, settings.serviceChargePercentage)
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    fun money(amount: Long) = posMoney(amount, settings.currencySymbol)
    Column(modifier.then(if (compact) Modifier else Modifier.fillMaxHeight()).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Keranjang", fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                Text("${quantityLabel(cart.totalItemCount)} barang · ${cart.items.size} produk", fontSize = 14.sp, lineHeight = 18.sp, color = colors.onSurfaceVariant)
            }
            if (cart.items.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text("Kosongkan", color = colors.error, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(14.dp)); HorizontalDivider()
        LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = !compact)) {
            item {
                Surface(onClick = onOpenCustomerPicker, color = colors.surface) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(CashierIcons.Person, null, Modifier.size(30.dp), tint = colors.onSurfaceVariant)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cart.customer?.name ?: "Pilih pelanggan", fontSize = 16.sp, lineHeight = 20.sp)
                            Text(if (cart.customer == null) "Opsional" else cart.customer.phone.orEmpty(), fontSize = 12.sp, lineHeight = 16.sp, color = colors.onSurfaceVariant)
                        }
                        if (cart.customer != null) IconButton(onClick = onRemoveCustomer) { Icon(Icons.Default.Close, "Lepas pelanggan") }
                        else Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
            }
            if (cart.items.isEmpty()) item { Text("Keranjang masih kosong", Modifier.padding(vertical = 32.dp), color = colors.onSurfaceVariant) }
            items(cart.items, key = { it.cartItemId }) { item ->
                CartItemRow(item, settings.currencySymbol, onUpdateQuantity, onRemoveItem)
                HorizontalDivider()
            }
            onAddProducts?.let { add -> item {
                Surface(onClick = add, color = colors.surface) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                        RoundQuantityAction(Icons.Default.Add, "Kembali ke katalog", add)
                        Spacer(Modifier.width(6.dp))
                        Text("Tambah barang", color = colors.primary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                HorizontalDivider()
            } }
            if (cart.items.isNotEmpty()) item {
                Column(Modifier.padding(top = 12.dp)) {
                    SummaryLine("Subtotal", money(calc.subtotal))
                    Surface(onClick = onOpenDiscountDialog, color = colors.surface) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Sell, null, Modifier.size(23.dp)); Spacer(Modifier.width(14.dp))
                            Text(if (calc.discount > 0) "Diskon" else "Tambah diskon", color = colors.onSurfaceVariant, modifier = Modifier.weight(1f), fontSize = 15.sp)
                            if (calc.discount > 0) Text("−${money(calc.discount)}", fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                        }
                    }
                    if (!cart.discountReason.isNullOrBlank()) Text(cart.discountReason, fontSize = 12.sp, lineHeight = 16.sp, color = colors.onSurfaceVariant)
                    if (calc.serviceCharge > 0) SummaryLine("Biaya layanan", money(calc.serviceCharge))
                    if (settings.isTaxEnabled && calc.tax > 0) SummaryLine(if (settings.isTaxInclusive) "Pajak (termasuk)" else "Pajak", money(calc.tax))
                }
            }
        }
        if (cart.items.isNotEmpty()) {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Total bayar", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(money(calc.grandTotal), fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onHoldCart, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary), border = BorderStroke(1.dp, colors.primary), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.Pause, null, Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)); Text("Tahan", fontSize = 16.sp)
                }
                Button(onClick = onCheckout, modifier = Modifier.weight(1.65f).heightIn(min = 48.dp), shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Bayar ${money(calc.grandTotal)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                }
            }
        }
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Kosongkan keranjang?") },
        text = { Text("Semua barang di pesanan ini akan dihapus dari keranjang.") },
        confirmButton = { TextButton(onClick = { confirmClear = false; onClearCart() }) { Text("Kosongkan", color = colors.error) } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Batal") } })
}
@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(label, Modifier.weight(1f), fontSize = 15.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun CartItemRow(item: CartItem, currencySymbol: String, onUpdateQuantity: (String, Double) -> Unit, onRemoveItem: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        ProductPhoto(item.product.imageUrl, item.product.name, Modifier.width(76.dp).height(82.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.product.name, fontSize = 15.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    item.variant?.let { Text(it.name, fontSize = 12.sp, lineHeight = 16.sp, color = colors.onSurfaceVariant) }
                }
                Spacer(Modifier.width(4.dp))
                Text(posMoney(item.totalPrice, currencySymbol), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("${posMoney(item.unitPrice, currencySymbol)} / ${item.product.unit}", fontSize = 13.sp, lineHeight = 18.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(item.quantity, item.product.name, { onUpdateQuantity(item.cartItemId, -1.0) },
                    { onUpdateQuantity(item.cartItemId, 1.0) }, Modifier.width(136.dp), canDecrease = item.quantity > 1)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onRemoveItem(item.cartItemId) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, "Hapus ${item.product.name}", tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
