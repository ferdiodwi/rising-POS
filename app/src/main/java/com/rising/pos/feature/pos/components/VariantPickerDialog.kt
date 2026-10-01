package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

/**
 * Dialog pemilih varian produk.
 *
 * Setiap varian adalah kartu yang bisa ditekan: nama, harga akhir (tabular),
 * dan status stok dengan ikon bila menipis atau habis. Varian tanpa stok
 * ditandai jelas dan tidak bisa dipilih.
 */
@Composable
fun VariantPickerDialog(
    product: ProductEntity,
    variants: List<ProductVariantEntity>,
    currencySymbol: String,
    onSelectVariant: (ProductVariantEntity) -> Unit,
    onDismiss: () -> Unit
) {
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
                    imageVector = Icons.Outlined.Sell,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text("Pilih varian", style = MaterialTheme.typography.titleMedium, color = Slate900)
                    Text(
                        product.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            if (variants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Produk ini belum punya varian.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(variants, key = { it.id }) { variant ->
                        val finalPrice = product.sellingPrice + variant.priceAdjustment
                        val outOfStock = product.trackStock && variant.stock != null && variant.stock <= 0

                        Card(
                            onClick = { if (!outOfStock) onSelectVariant(variant) },
                            enabled = !outOfStock,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.dp, Slate200),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        variant.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (outOfStock) Slate500 else Slate900
                                    )
                                    if (product.trackStock && variant.stock != null) {
                                        StockNote(
                                            outOfStock = outOfStock,
                                            label = "Stok ${variant.stock.toInt()} ${product.unit}"
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    CurrencyFormatter.format(finalPrice, currencySymbol),
                                    style = PosTextStyles.priceCard,
                                    color = if (outOfStock) Slate500 else Slate900
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
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
