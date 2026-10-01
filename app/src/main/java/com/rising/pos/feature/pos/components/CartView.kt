package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

/**
 * Isi keranjang: daftar item, pemilih pelanggan, ringkasan uang, aksi bayar.
 *
 * Dipakai di dua tempat: panel kanan (tablet) dan bottom sheet (HP), jadi
 * komponen ini mengisi tinggi penuh induknya.
 */
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
    modifier: Modifier = Modifier
) {
    val calc = cart.calculateTotals(
        isTaxEnabled = settings.isTaxEnabled,
        taxPercentage = settings.taxPercentage,
        isTaxInclusive = settings.isTaxInclusive,
        isServiceChargeEnabled = settings.isServiceChargeEnabled,
        serviceChargePercentage = settings.serviceChargePercentage
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Judul + aksi kosongkan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pesanan", style = MaterialTheme.typography.titleLarge, color = Slate900)
                if (cart.items.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = SuccessGreen.copy(alpha = 0.12f), shape = CircleShape) {
                        Text(
                            text = "${cart.totalItemCount.toInt()} item",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            if (cart.items.isNotEmpty()) {
                TextButton(onClick = onClearCart) {
                    Text(
                        text = "Kosongkan",
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Pemilih pelanggan
        CustomerRow(cart = cart, onOpenCustomerPicker = onOpenCustomerPicker, onRemoveCustomer = onRemoveCustomer)

        HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 12.dp))

        // Daftar item atau empty state
        if (cart.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Keranjang masih kosong",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate700
                    )
                    Text(
                        text = "Pilih produk dari katalog untuk mulai membuat pesanan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(cart.items, key = { it.cartItemId }) { item ->
                    CartItemRow(
                        item = item,
                        currencySymbol = settings.currencySymbol,
                        onUpdateQuantity = onUpdateQuantity,
                        onRemoveItem = onRemoveItem
                    )
                }
            }
        }

        // Ringkasan + aksi
        if (cart.items.isNotEmpty()) {
            HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryLine(
                    label = "Subtotal",
                    value = CurrencyFormatter.format(calc.subtotal, settings.currencySymbol)
                )

                DiscountLine(
                    calc = calc,
                    cart = cart,
                    settings = settings,
                    onOpenDiscountDialog = onOpenDiscountDialog
                )

                if (calc.serviceCharge > 0) {
                    SummaryLine(
                        label = "Biaya layanan",
                        value = CurrencyFormatter.format(calc.serviceCharge, settings.currencySymbol)
                    )
                }

                if (settings.isTaxEnabled && calc.tax > 0) {
                    val taxLabel = if (settings.isTaxInclusive) {
                        "Pajak (${settings.taxPercentage}% termasuk)"
                    } else {
                        "Pajak (${settings.taxPercentage}%)"
                    }
                    SummaryLine(
                        label = taxLabel,
                        value = CurrencyFormatter.format(calc.tax, settings.currencySymbol)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Total bayar: angka paling menonjol di layar ini.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total bayar", style = MaterialTheme.typography.titleMedium, color = Slate900)
                    Text(
                        text = CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                        style = PosTextStyles.displayMoney,
                        color = Slate900
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onHoldCart,
                        modifier = Modifier
                            .weight(0.4f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Tahan", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    }

                    Button(
                        onClick = onCheckout,
                        modifier = Modifier
                            .weight(0.6f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            Icons.Outlined.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Bayar",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRow(
    cart: CartState,
    onOpenCustomerPicker: () -> Unit,
    onRemoveCustomer: () -> Unit
) {
    val customer = cart.customer
    if (customer != null) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable { onOpenCustomerPicker() }
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onRemoveCustomer, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Lepas pelanggan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable { onOpenCustomerPicker() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Pilih pelanggan",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate700
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate500)
        Text(value, style = PosTextStyles.money, color = Slate900)
    }
}

@Composable
private fun DiscountLine(
    calc: com.rising.pos.domain.model.CartCalculation,
    cart: CartState,
    settings: BusinessSettings,
    onOpenDiscountDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onOpenDiscountDialog),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (calc.discount > 0) {
            Column {
                Text(
                    text = "Diskon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DangerRed,
                    fontWeight = FontWeight.SemiBold
                )
                if (!cart.discountReason.isNullOrBlank()) {
                    Text(
                        text = cart.discountReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
            Text(
                text = "- ${CurrencyFormatter.format(calc.discount, settings.currencySymbol)} · Ubah",
                style = PosTextStyles.money,
                color = DangerRed
            )
        } else {
            Text(
                text = "Tambah diskon",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "›", style = MaterialTheme.typography.titleMedium, color = Slate400)
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    currencySymbol: String,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.variant?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                Text(
                    text = "${CurrencyFormatter.format(item.unitPrice, currencySymbol)} / ${item.product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
            Text(
                text = CurrencyFormatter.format(item.totalPrice, currencySymbol),
                style = PosTextStyles.money,
                color = Slate900,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))

            QuantityButton(
                icon = if (item.quantity <= 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                contentDescription = if (item.quantity <= 1) {
                    "Hapus ${item.product.name}"
                } else {
                    "Kurangi ${item.product.name}"
                },
                tint = Slate500,
                onClick = {
                    if (item.quantity <= 1) onRemoveItem(item.cartItemId)
                    else onUpdateQuantity(item.cartItemId, -1.0)
                }
            )
            Text(
                text = item.quantity.toInt().toString(),
                style = MaterialTheme.typography.titleSmall,
                color = Slate900,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuantityButton(
                icon = Icons.Default.Add,
                contentDescription = "Tambah ${item.product.name}",
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onUpdateQuantity(item.cartItemId, 1.0) }
            )
        }

        HorizontalDivider(color = Slate200)
    }
}

@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}
