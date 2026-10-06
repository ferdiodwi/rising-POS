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
fun ProductPhoto(
    imageUrl: String?, name: String, modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
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
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
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
    modifier: Modifier = Modifier
) {
    val product = item.product
    // Bila "Pencatatan Stok" global nonaktif, stok diabaikan sepenuhnya sehingga
    // produk tidak pernah dianggap habis dan selalu bisa dijual.
    val unavailable = stockTrackingEnabled && product.trackStock && product.stock <= 0
    val selected = cartQuantity > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) PrimaryBlue else Slate200
        ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // 1. Photo with quantity badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clickable(enabled = !unavailable, onClick = onClick)
            ) {
                ProductPhoto(
                    imageUrl = product.imageUrl,
                    name = product.name,
                    modifier = Modifier.fillMaxSize()
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(PrimaryBlue, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${quantityLabel(cartQuantity)}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. Product Name
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = !unavailable, onClick = onClick)
            )

            Spacer(Modifier.height(2.dp))

            // 3. Price & Stock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = posMoney(product.sellingPrice, currencySymbol),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) PrimaryBlue else Slate900
                )
                if (unavailable) {
                    Text(
                        text = "Stok habis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DangerRed
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 4. Uniform Action Slot (always 34.dp height for both selected & unselected)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                if (selected) {
                    // Stepper: [-]  qty  [+]
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlueContainer,
                        border = BorderStroke(1.dp, Slate200)
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
                                modifier = Modifier.size(28.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Kurangi ${product.name}",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = quantityLabel(cartQuantity),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            IconButton(
                                onClick = onIncrease,
                                enabled = !unavailable,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (!unavailable) PrimaryBlue else Slate400,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Tambah ${product.name}",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Unselected: "+ Tambah" button in the same 34dp slot
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !unavailable, onClick = onIncrease),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Tambah",
                                fontSize = 12.sp,
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
