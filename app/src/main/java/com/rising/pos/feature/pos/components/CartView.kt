package com.rising.pos.feature.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.OutlinedButton
import com.rising.pos.ui.theme.PrimaryBlue

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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pesanan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                if (cart.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${cart.totalItemCount.toInt()} item",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (cart.items.isNotEmpty()) {
                TextButton(onClick = onClearCart) {
                    Text(
                        text = "Kosongkan",
                        style = MaterialTheme.typography.labelMedium.copy(color = DangerRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cart.customer != null) {
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.heightIn(min = 48.dp).clickable { onOpenCustomerPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cart.customer.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onRemoveCustomer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Lepas Pelanggan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = Slate200.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.heightIn(min = 48.dp).clickable { onOpenCustomerPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Pilih Pelanggan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Slate700
                            )
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = Slate200,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Item List or Empty State
        if (cart.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Keranjang Masih Kosong",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    )
                    Text(
                        text = "Pilih produk dari katalog untuk mulai membuat pesanan.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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

        // Summary & Checkout Button
        if (cart.items.isNotEmpty()) {
            HorizontalDivider(
                color = Slate200,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium.copy(color = Slate500))
                    Text(
                        CurrencyFormatter.format(calc.subtotal, settings.currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                    )
                }

                // Discount Row (Clickable)
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
                                "Diskon",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DangerRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (!cart.discountReason.isNullOrBlank()) {
                                Text(
                                    cart.discountReason,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                )
                            }
                        }
                        Text(
                            "- ${CurrencyFormatter.format(calc.discount, settings.currencySymbol)} (Ubah)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        )
                    } else {
                        Text(
                            "+ Tambah Diskon",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            "-",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }
                }

                if (calc.serviceCharge > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biaya layanan", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(CurrencyFormatter.format(calc.serviceCharge, settings.currencySymbol),
                            style = MaterialTheme.typography.bodySmall, color = Slate700)
                    }
                }

                if (settings.isTaxEnabled && calc.tax > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val taxLabel = if (settings.isTaxInclusive) "Pajak (${settings.taxPercentage}% Termasuk)" else "Pajak (${settings.taxPercentage}%)"
                        Text(taxLabel, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                        Text(
                            CurrencyFormatter.format(calc.tax, settings.currencySymbol),
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Bayar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onHoldCart,
                        modifier = Modifier
                            .weight(0.38f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCheckout,
                        modifier = Modifier
                            .weight(0.62f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bayar",
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

@Composable
private fun CartItemRow(
    item: CartItem,
    currencySymbol: String,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(item.product.name, style = MaterialTheme.typography.titleSmall, color = Slate900)
        item.variant?.let { Text(it.name, style = MaterialTheme.typography.bodySmall, color = Slate500) }
        Text(CurrencyFormatter.format(item.unitPrice, currencySymbol) + " / " + item.product.unit,
            style = MaterialTheme.typography.bodySmall, color = Slate500)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(CurrencyFormatter.format(item.totalPrice, currencySymbol),
                style = MaterialTheme.typography.titleSmall, color = Slate900, modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    if (item.quantity <= 1) onRemoveItem(item.cartItemId)
                    else onUpdateQuantity(item.cartItemId, -1.0)
                }, modifier = Modifier.size(48.dp)
            ) {
                Icon(if (item.quantity <= 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                    if (item.quantity <= 1) "Hapus ${item.product.name}" else "Kurangi ${item.product.name}",
                    tint = Slate500, modifier = Modifier.size(20.dp))
            }
            Text(item.quantity.toInt().toString(), style = MaterialTheme.typography.titleSmall, color = Slate900,
                modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onUpdateQuantity(item.cartItemId, 1.0) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, "Tambah ${item.product.name}", tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
            }
        }
        HorizontalDivider(color = Slate200)
    }
}
