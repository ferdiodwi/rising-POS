package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate800
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

/**
 * Dialog kustomisasi produk: Varian & Topping / Modifier tambahan (F&B).
 *
 * Bila produk hanya punya varian (tanpa modifier), kasir dapat langsung 1-klik varian
 * untuk masuk keranjang secara cepat.
 * Bila ada modifier / topping, kasir dapat memilih varian dan multi-select topping,
 * dengan kalkulasi harga total berjalan dinamis.
 */
@Composable
fun VariantPickerDialog(
    product: ProductEntity,
    variants: List<ProductVariantEntity>,
    modifiers: List<ModifierEntity> = emptyList(),
    currencySymbol: String,
    onConfirm: (ProductVariantEntity?, List<ModifierEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVariant by remember(product.id) {
        mutableStateOf(variants.firstOrNull { !(product.trackStock && it.stock != null && it.stock <= 0) })
    }
    var selectedModifierIds by remember(product.id) {
        mutableStateOf(emptySet<String>())
    }

    val currentVariantAdjustment = selectedVariant?.priceAdjustment ?: 0L
    val selectedModifiersList = remember(selectedModifierIds, modifiers) {
        modifiers.filter { selectedModifierIds.contains(it.id) }
    }
    val currentModifiersTotal = selectedModifiersList.sumOf { it.price }
    val totalPrice = product.sellingPrice + currentVariantAdjustment + currentModifiersTotal

    val canSubmit = variants.isEmpty() || selectedVariant != null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (modifiers.isNotEmpty()) Icons.Outlined.Tune else Icons.Outlined.Sell,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (modifiers.isNotEmpty()) "Kustomisasi Pesanan" else "Pilih Varian",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)
            ) {
                // ── Bagian 1: Pilihan Varian (jika ada) ──
                if (variants.isNotEmpty()) {
                    item {
                        Text(
                            text = "PILIH VARIAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(variants, key = { it.id }) { variant ->
                        val finalPrice = product.sellingPrice + variant.priceAdjustment
                        val outOfStock = product.trackStock && variant.stock != null && variant.stock <= 0
                        val isSelected = selectedVariant?.id == variant.id

                        Card(
                            onClick = {
                                if (!outOfStock) {
                                    selectedVariant = variant
                                    // Jika tidak ada modifier, 1-klik langsung masuk keranjang
                                    if (modifiers.isEmpty()) {
                                        onConfirm(variant, emptyList())
                                    }
                                }
                            },
                            enabled = !outOfStock,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryBlue else Slate200
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(
                                            text = variant.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (outOfStock) Slate500 else if (isSelected) PrimaryBlue else Slate900
                                        )
                                        if (product.trackStock && variant.stock != null) {
                                            StockNote(
                                                outOfStock = outOfStock,
                                                label = "Stok ${variant.stock.toInt()} ${product.unit}"
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    text = CurrencyFormatter.format(finalPrice, currencySymbol),
                                    style = PosTextStyles.priceCard,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (outOfStock) Slate500 else if (isSelected) PrimaryBlue else Slate900
                                )
                            }
                        }
                    }
                }

                // ── Bagian 2: Pilihan Topping / Modifier (jika ada) ──
                if (modifiers.isNotEmpty()) {
                    item {
                        if (variants.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = Slate200, thickness = 1.dp)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = "PILIHAN TOPPING & TAMBAHAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 0.5.sp
                        )
                    }

                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            modifiers.forEach { mod ->
                                val isSelected = selectedModifierIds.contains(mod.id)
                                Surface(
                                    onClick = {
                                        selectedModifierIds = if (isSelected) {
                                            selectedModifierIds - mod.id
                                        } else {
                                            if (!mod.isMultipleSelect) {
                                                // Jika single select, hapus yang lain dari grup ini jika ada, atau toggle
                                                selectedModifierIds + mod.id
                                            } else {
                                                selectedModifierIds + mod.id
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PrimaryBlue else Slate200
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Column {
                                            Text(
                                                text = mod.name,
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) PrimaryBlue else Slate800
                                                )
                                            )
                                            Text(
                                                text = if (mod.price > 0) "+${CurrencyFormatter.format(mod.price, currencySymbol)}" else "Gratis / Opsi",
                                                style = TextStyle(
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) PrimaryBlue else Slate500
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (modifiers.isNotEmpty() || variants.isNotEmpty()) {
                Button(
                    onClick = {
                        if (canSubmit) {
                            onConfirm(selectedVariant, selectedModifiersList)
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Tambah • ${CurrencyFormatter.format(totalPrice, currencySymbol)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
            }
        }
    )
}

@Composable
private fun StockNote(outOfStock: Boolean, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (outOfStock) {
            Icon(
                imageVector = Icons.Outlined.Block,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = if (outOfStock) "Stok habis" else label,
            style = MaterialTheme.typography.labelMedium,
            color = if (outOfStock) DangerRed else SuccessGreen
        )
    }
}
