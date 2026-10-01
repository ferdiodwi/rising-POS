package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.*

@Composable
fun ProductCard(item: ProductWithCategory, currencySymbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val product = item.product
    val unavailable = product.trackStock && product.stock <= 0
    val lowStock = product.trackStock && product.stock > 0 && product.stock <= product.minStock
    Card(
        onClick = onClick, enabled = !unavailable,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate200),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.category?.name ?: "Tanpa kategori", style = MaterialTheme.typography.labelSmall,
                color = Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.name, style = MaterialTheme.typography.titleMedium,
                color = if (unavailable) Slate500 else Slate900,
                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(CurrencyFormatter.format(product.sellingPrice, currencySymbol),
                style = MaterialTheme.typography.titleMedium, color = Slate900)
            HorizontalDivider(color = Slate200)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        unavailable -> "Stok habis"
                        product.trackStock -> "Sisa ${product.stock.toInt()} ${product.unit}"
                        else -> "Tambah produk"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when { unavailable -> DangerRed; lowStock -> WarningAmber; else -> Slate500 },
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Add, null, tint = if (unavailable) Slate500 else PrimaryBlue,
                    modifier = Modifier.size(22.dp))
            }
        }
    }
}
