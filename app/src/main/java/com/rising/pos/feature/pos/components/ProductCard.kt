package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.ui.components.CashierIcons
import com.rising.pos.ui.components.quantityLabel
import com.rising.pos.ui.theme.posMoney
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.PrimaryBlueContainer
import com.rising.pos.ui.theme.DangerRed

@Composable
fun ProductPhoto(imageUrl: String?, name: String, modifier: Modifier = Modifier) {
    var failed by remember(imageUrl) { mutableStateOf(false) }
    var loaded by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (loaded) Modifier.testTag("loaded_$name") else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank() || failed) {
            Icon(
                imageVector = CashierIcons.Box,
                contentDescription = "Foto $name belum tersedia",
                modifier = Modifier.size(36.dp),
                tint = Slate400
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                onSuccess = { loaded = true },
                onError = { failed = true }
            )
        }
    }
}

@Composable
fun ProductCard(
    item: ProductWithCategory,
    currencySymbol: String,
    cartQuantity: Double = 0.0,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit = {},
    onClick: () -> Unit = onIncrease,
    stockTrackingEnabled: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val product = item.product
    // Bila "Pencatatan Stok" global nonaktif, stok diabaikan sepenuhnya sehingga
    // produk tidak pernah dianggap habis dan selalu bisa dijual.
    val unavailable = stockTrackingEnabled && product.trackStock && product.stock <= 0
    val selected = cartQuantity > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) PrimaryBlue else Slate200
        ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(if (compact) 6.dp else 8.dp)) {
            // 1. Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 58.dp else 104.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(com.rising.pos.ui.theme.Slate50)
                    .clickable(enabled = !unavailable, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                ProductPhoto(
                    imageUrl = product.imageUrl,
                    name = product.name,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(if (compact) 4.dp else 8.dp))

            // 2. Product Name
            Text(
                text = product.name,
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = !unavailable, onClick = onClick)
            )

            Spacer(Modifier.height(if (compact) 1.dp else 2.dp))

            // 3. Price & Stock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = posMoney(product.sellingPrice, currencySymbol),
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                if (unavailable) {
                    Text(
                        text = "Stok habis",
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DangerRed
                    )
                }
            }

            Spacer(Modifier.height(if (compact) 4.dp else 8.dp))

            // 4. Uniform Action Slot
            val actionHeight = if (compact) 28.dp else 34.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(actionHeight)
            ) {
                if (selected) {
                    // Stepper: [-]  qty  [+]
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onDecrease,
                                modifier = Modifier.size(if (compact) 26.dp else 30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurangi ${product.name}",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(if (compact) 13.dp else 16.dp)
                                )
                            }

                            Text(
                                text = quantityLabel(cartQuantity),
                                fontSize = if (compact) 12.sp else 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            IconButton(
                                onClick = onIncrease,
                                enabled = !unavailable,
                                modifier = Modifier.size(if (compact) 26.dp else 30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah ${product.name}",
                                    tint = if (!unavailable) PrimaryBlue else Slate400,
                                    modifier = Modifier.size(if (compact) 13.dp else 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Unselected: "+ Tambah" button in the same action slot
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !unavailable, onClick = onIncrease),
                        shape = RoundedCornerShape(8.dp),
                        color = com.rising.pos.ui.theme.Slate50,
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah ${product.name}",
                                tint = if (!unavailable) PrimaryBlue else Slate400,
                                modifier = Modifier.size(if (compact) 13.dp else 15.dp)
                            )
                            Spacer(Modifier.width(if (compact) 2.dp else 4.dp))
                            Text(
                                text = "Tambah",
                                fontSize = if (compact) 12.sp else 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!unavailable) PrimaryBlue else Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}
