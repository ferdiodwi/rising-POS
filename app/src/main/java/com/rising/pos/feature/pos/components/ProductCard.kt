package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.ui.components.*
import com.rising.pos.ui.theme.posMoney

@Composable
fun ProductPhoto(imageUrl: String?, name: String, modifier: Modifier = Modifier) {
    var failed by remember(imageUrl) { mutableStateOf(false) }
    var loaded by remember(imageUrl) { mutableStateOf(false) }
    Box(modifier.then(if (loaded) Modifier.testTag("loaded_$name") else Modifier), contentAlignment = Alignment.Center) {
        if (imageUrl.isNullOrBlank() || failed) Icon(CashierIcons.Box, "Foto $name belum tersedia", Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outline)
        else AsyncImage(model = imageUrl, contentDescription = name, contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(), onSuccess = { loaded = true }, onError = { failed = true })
    }
}
@Composable
fun ProductCard(item: ProductWithCategory, currencySymbol: String, cartQuantity: Double = 0.0,
    onIncrease: () -> Unit, onDecrease: () -> Unit = {}, onClick: () -> Unit = onIncrease, modifier: Modifier = Modifier
) {
    val product = item.product
    val unavailable = product.trackStock && product.stock <= 0
    val selected = cartQuantity > 0
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(7.dp),
        border = BorderStroke(if (selected) 1.25.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant), color = colors.surface) {
        Column(Modifier.padding(6.dp)) {
            ProductPhoto(product.imageUrl, product.name, Modifier.fillMaxWidth().height(76.dp).padding(2.dp).clickable(enabled = !unavailable, onClick = onClick))
            if (selected) {
                Text(product.name, fontSize = 14.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp).padding(top = 4.dp).clickable(onClick = onClick))
                Text(posMoney(product.sellingPrice, currencySymbol), fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                Spacer(Modifier.height(6.dp))
                QuantityStepper(cartQuantity, product.name, onDecrease, onIncrease, Modifier.fillMaxWidth(), canIncrease = !unavailable, catalog = true)
            } else {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f).padding(start = 6.dp).clickable(enabled = !unavailable, onClick = onClick)) {
                        Text(product.name, fontSize = 14.sp, lineHeight = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(posMoney(product.sellingPrice, currencySymbol), fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        if (unavailable) Text("Stok habis", fontSize = 11.sp, lineHeight = 14.sp, color = colors.error)
                    }
                    RoundQuantityAction(Icons.Default.Add, "Tambah ${product.name}", onIncrease, !unavailable)
                }
            }
        }
    }
}
