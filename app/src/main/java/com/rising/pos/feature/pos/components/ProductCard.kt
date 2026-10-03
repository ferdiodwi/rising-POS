package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedButton
import com.rising.pos.ui.components.QuantityStepper
import com.rising.pos.ui.theme.PosTextStyles
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900

/**
 * Kartu produk kasir dengan gaya visual modern clean:
 * - Menampilkan gambar/ilustrasi kategori produk di bagian atas
 * - Nama produk & harga
 * - Ketika item sudah di keranjang (quantity > 0): border biru aktif 1.5dp dan stepper - qty +
 * - Ketika item belum di keranjang (quantity == 0): border 1dp Slate200 dan tombol + bulat biru di kanan
 */
@Composable
fun ProductCard(
    item: ProductWithCategory,
    currencySymbol: String,
    cartQuantity: Double = 0.0,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit = {},
    onClick: () -> Unit = onIncrease,
    modifier: Modifier = Modifier
) {
    val product = item.product
    val unavailable = product.trackStock && product.stock <= 0

    val isSelectedInCart = cartQuantity > 0
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelectedInCart) primaryColor else Slate200
    val borderWidth = if (isSelectedInCart) 1.5.dp else 1.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Bagian atas (gambar, nama, harga) dapat diklik untuk memilih / melihat varian
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !unavailable) { onClick() }
            ) {
                // Area Gambar / Ilustrasi Produk
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelectedInCart) primaryColor.copy(alpha = 0.04f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val categoryName = item.category?.name?.lowercase().orEmpty()
                    val icon = when {
                        categoryName.contains("minum") -> Icons.Outlined.LocalDrink
                        categoryName.contains("kopi") || categoryName.contains("kafe") -> Icons.Outlined.Coffee
                        categoryName.contains("makan") || categoryName.contains("snack") || categoryName.contains("camil") -> Icons.Outlined.Fastfood
                        categoryName.contains("sembako") || categoryName.contains("retail") -> Icons.Outlined.ShoppingBag
                        else -> Icons.Outlined.Inventory2
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelectedInCart) primaryColor else Slate400,
                        modifier = Modifier.size(46.dp)
                    )

                    if (unavailable) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Stok Habis",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Nama Produk
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = if (unavailable) Slate500 else Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Harga
                Text(
                    text = CurrencyFormatter.format(product.sellingPrice, currencySymbol),
                    style = PosTextStyles.priceCard,
                    color = if (unavailable) Slate500 else Slate900
                )
            }

            Spacer(Modifier.height(8.dp))

            if (isSelectedInCart) {
                QuantityStepper(
                    quantity = cartQuantity,
                    productName = product.name,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease,
                    canIncrease = !unavailable,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedButton(
                    onClick = onIncrease,
                    enabled = !unavailable,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (unavailable) "Habis" else "Tambah")
                }
            }
        }
    }
}
