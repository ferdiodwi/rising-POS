package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.ui.components.quantityLabel
import com.rising.pos.ui.theme.posMoney

/** Tablet-only tile: contained photography and full-width rectangular controls. */
@Composable
internal fun TabletProductCard(
    item: ProductWithCategory,
    currencySymbol: String,
    cartQuantity: Double,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    stockTrackingEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val product = item.product
    val unavailable = stockTrackingEnabled && product.trackStock && product.stock <= 0
    val selected = cartQuantity > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant)
    ) {
        Column(Modifier.padding(10.dp)) {
            ProductPhoto(
                imageUrl = product.imageUrl,
                name = product.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.65f)
                    .clickable(enabled = !unavailable, role = Role.Button, onClick = onIncrease),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                product.name,
                modifier = Modifier.fillMaxWidth().clickable(enabled = !unavailable, onClick = onIncrease),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(posMoney(product.sellingPrice, currencySymbol), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (selected) {
                TabletQuantityStepper(
                    quantity = cartQuantity,
                    productName = product.name,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                    canIncrease = !unavailable,
                    highlighted = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Surface(
                    onClick = onIncrease,
                    enabled = !unavailable,
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceContainerLow,
                    contentColor = if (unavailable) colors.onSurfaceVariant else colors.primary,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!unavailable) {
                            Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(if (unavailable) "Stok habis" else "Tambah", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Shared by tablet tiles and cart lines; actions use the real cart callbacks. */
@Composable
internal fun TabletQuantityStepper(
    quantity: Double,
    productName: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    canIncrease: Boolean = true,
    highlighted: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (highlighted) colors.primaryContainer else colors.surface,
        border = BorderStroke(1.dp, if (highlighted) colors.primary.copy(alpha = 0.3f) else colors.outlineVariant)
    ) {
        Row(Modifier.heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Remove, "Kurangi $productName", tint = colors.primary)
            }
            VerticalDivider(Modifier.height(30.dp), color = colors.outlineVariant)
            Box(Modifier.weight(1f).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                Text(quantityLabel(quantity), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            VerticalDivider(Modifier.height(30.dp), color = colors.outlineVariant)
            IconButton(onClick = onIncrease, enabled = canIncrease, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Add, "Tambah $productName", tint = if (canIncrease) colors.primary else colors.onSurfaceVariant)
            }
        }
    }
}
