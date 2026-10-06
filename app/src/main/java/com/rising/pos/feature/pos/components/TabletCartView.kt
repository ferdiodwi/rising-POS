package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.BusinessType
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.components.CashierIcons
import com.rising.pos.ui.components.quantityLabel
import com.rising.pos.ui.theme.posMoney

/** Permanent tablet cart. Only its list scrolls; totals stay beside the catalog. */
@Composable
internal fun TabletCartView(
    cart: CartState,
    settings: BusinessSettings,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onHoldCart: () -> Unit,
    onOpenCustomerPicker: () -> Unit,
    onRemoveCustomer: () -> Unit,
    onOpenTablePicker: () -> Unit,
    onRemoveTable: () -> Unit,
    onOpenDiscountDialog: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val totals = cart.calculateTotals(
        settings.isTaxEnabled, settings.taxPercentage, settings.isTaxInclusive,
        settings.isServiceChargeEnabled, settings.serviceChargePercentage
    )
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    fun money(value: Long) = posMoney(value, settings.currencySymbol)

    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        // On a short window/with the keyboard open, the summary can scroll too.
        val summaryMaxHeight = maxHeight * 0.52f
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Keranjang", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${quantityLabel(cart.totalItemCount)} barang · ${cart.items.size} produk",
                        fontSize = 14.sp, color = colors.onSurfaceVariant
                    )
                }
                TextButton(onClick = { confirmClear = true }, enabled = cart.items.isNotEmpty()) {
                    Text("Kosongkan", color = if (cart.items.isEmpty()) colors.onSurfaceVariant else colors.error)
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                item(key = "customer") {
                    TabletCartSelection(
                        icon = CashierIcons.Person,
                        title = cart.customer?.name ?: "Pilih pelanggan",
                        subtitle = cart.customer?.phone?.takeIf { it.isNotBlank() } ?: if (cart.customer == null) "Opsional" else "Pelanggan terpilih",
                        onClick = onOpenCustomerPicker,
                        onRemove = if (cart.customer != null) onRemoveCustomer else null,
                        removeLabel = "Lepas pelanggan"
                    )
                }
                if (settings.isTableEnabled || settings.type == BusinessType.CAFE) {
                    item(key = "table") {
                        TabletCartSelection(
                            icon = Icons.Outlined.TableRestaurant,
                            title = cart.tableNumber ?: "Pilih meja",
                            subtitle = "Makan di tempat",
                            onClick = onOpenTablePicker,
                            onRemove = if (cart.tableNumber != null) onRemoveTable else null,
                            removeLabel = "Lepas meja"
                        )
                    }
                }
                if (cart.items.isEmpty()) {
                    item(key = "empty") {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.ShoppingCart, null, Modifier.size(36.dp), tint = colors.onSurfaceVariant)
                            Text("Keranjang masih kosong", fontWeight = FontWeight.SemiBold)
                            Text("Pilih barang dari katalog di sebelah kiri.", color = colors.onSurfaceVariant,
                                fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                items(cart.items, key = { it.cartItemId }) { item ->
                    TabletCartItemRow(item, settings.currencySymbol, onUpdateQuantity, onRemoveItem)
                    HorizontalDivider(color = colors.outlineVariant)
                }
            }
            Column(
                Modifier.fillMaxWidth().heightIn(max = summaryMaxHeight)
                    .verticalScroll(rememberScrollState()).padding(bottom = 16.dp)
            ) {
                HorizontalDivider(color = colors.outlineVariant)
                Spacer(Modifier.height(12.dp))
                TabletSummaryLine("Subtotal", money(totals.subtotal))
                Surface(onClick = onOpenDiscountDialog, enabled = cart.items.isNotEmpty(), color = colors.surface) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Sell, null, Modifier.size(22.dp), tint = colors.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        Text(if (totals.discount > 0) "Diskon" else "Tambah diskon", Modifier.weight(1f),
                            color = colors.onSurfaceVariant, fontSize = 14.sp)
                        if (totals.discount > 0) Text("−${money(totals.discount)}", fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                    }
                }
                cart.discountReason?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (totals.serviceCharge > 0) TabletSummaryLine("Biaya layanan", money(totals.serviceCharge))
                if (settings.isTaxEnabled && totals.tax > 0) {
                    TabletSummaryLine(if (settings.isTaxInclusive) "Pajak (termasuk)" else "Pajak", money(totals.tax))
                }
                HorizontalDivider(color = colors.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Total bayar", Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(money(totals.grandTotal), Modifier.weight(1.4f), fontSize = 24.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onHoldCart, enabled = cart.items.isNotEmpty(),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (cart.items.isEmpty()) colors.outlineVariant else colors.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Pause, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tahan", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onCheckout, enabled = cart.items.isNotEmpty(),
                        modifier = Modifier.weight(2.1f).heightIn(min = 52.dp),
                        shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text("Bayar ${money(totals.grandTotal)}", Modifier.weight(1f), fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
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
                TextButton(onClick = { confirmClear = false; onClearCart() }) {
                    Text("Kosongkan", color = colors.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun TabletCartSelection(
    icon: ImageVector, title: String, subtitle: String,
    onClick: () -> Unit, onRemove: (() -> Unit)?, removeLabel: String
) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, color = colors.surface) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(28.dp), tint = colors.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 13.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (onRemove != null) IconButton(onClick = onRemove) { Icon(Icons.Default.Close, removeLabel) }
            else Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
        }
    }
    HorizontalDivider(color = colors.outlineVariant)
}

@Composable
private fun TabletCartItemRow(
    item: CartItem, currencySymbol: String,
    onUpdateQuantity: (String, Double) -> Unit, onRemoveItem: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.Top) {
        ProductPhoto(item.product.imageUrl, item.product.name, Modifier.size(64.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(item.product.name, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(8.dp))
                Text(posMoney(item.totalPrice, currencySymbol), Modifier.widthIn(max = 120.dp),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            item.variant?.let { Text(it.name, fontSize = 12.sp, color = colors.onSurfaceVariant) }
            if (item.selectedModifiers.isNotEmpty()) {
                Text(item.selectedModifiers.joinToString(", ") { it.name }, fontSize = 12.sp,
                    color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("${posMoney(item.unitPrice, currencySymbol)} / ${item.product.unit}",
                fontSize = 13.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TabletQuantityStepper(
                    quantity = item.quantity, productName = item.product.name,
                    onDecrease = { onUpdateQuantity(item.cartItemId, -1.0) },
                    onIncrease = { onUpdateQuantity(item.cartItemId, 1.0) },
                    modifier = Modifier.weight(1f).widthIn(max = 172.dp)
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { onRemoveItem(item.cartItemId) }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, "Hapus ${item.product.name}", tint = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TabletSummaryLine(label: String, amount: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Text(amount, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
