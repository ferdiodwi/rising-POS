package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.WarningAmber

/**
 * Kartu produk di katalog kasir.
 *
 * Hierarki: nama produk (2 baris) lalu harga (tebal, tabular) sebagai angka yang
 * paling menonjol. Status stok memakai IKON + TEKS, bukan hanya warna, supaya
 * tetap terbaca oleh pengguna dengan buta warna.
 *
 * State:
 * - Habis  : kartu diredupkan, tidak bisa ditekan, label "Stok habis" + ikon blok.
 * - Menipis: label amber + ikon peringatan.
 * - Aman   : label sisa stok netral.
 */
@Composable
fun ProductCard(
    item: ProductWithCategory,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val product = item.product
    val unavailable = product.trackStock && product.stock <= 0
    val lowStock = product.trackStock && product.stock > 0 && product.stock <= product.minStock

    Card(
        onClick = onClick,
        enabled = !unavailable,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 132.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.category?.name ?: "Tanpa kategori",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (unavailable) Slate500 else Slate900,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = CurrencyFormatter.format(product.sellingPrice, currencySymbol),
                style = PosTextStyles.priceCard,
                color = if (unavailable) Slate500 else Slate900
            )

            Spacer(Modifier.height(2.dp))

            StockStatus(
                unavailable = unavailable,
                lowStock = lowStock,
                trackStock = product.trackStock,
                stockLabel = "Sisa ${product.stock.toInt()} ${product.unit}"
            )
        }
    }
}

@Composable
private fun StockStatus(
    unavailable: Boolean,
    lowStock: Boolean,
    trackStock: Boolean,
    stockLabel: String
) {
    val (icon, label, color) = when {
        unavailable -> Triple(Icons.Outlined.Block, "Stok habis", DangerRed)
        lowStock -> Triple(Icons.Outlined.WarningAmber, "Menipis · $stockLabel", WarningAmber)
        trackStock -> Triple(null, stockLabel, Slate500)
        else -> Triple(null, "Tersedia", Slate500)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
