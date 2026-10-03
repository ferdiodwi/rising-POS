package com.rising.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rising.pos.ui.theme.PosTextStyles
import java.text.NumberFormat
import java.util.Locale

fun quantityLabel(quantity: Double): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 3
    }.format(quantity)

/** Separate 48dp targets, shared by product cards and the cart. */
@Composable
fun QuantityStepper(
    quantity: Double,
    productName: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrease: Boolean = true,
    canIncrease: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onDecrease, enabled = canDecrease, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Remove, "Kurangi $productName", modifier = Modifier.size(18.dp))
            }
            Text(quantityLabel(quantity), style = PosTextStyles.money, modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onIncrease, enabled = canIncrease, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, "Tambah $productName", modifier = Modifier.size(18.dp))
            }
        }
    }
}
