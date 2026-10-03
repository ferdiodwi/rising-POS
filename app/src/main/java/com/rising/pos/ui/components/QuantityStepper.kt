package com.rising.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

fun quantityLabel(quantity: Double): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
    maximumFractionDigits = 3
}.format(quantity)

@Composable
fun RoundQuantityAction(icon: ImageVector, description: String, onClick: () -> Unit, enabled: Boolean = true, outlined: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Surface(shape = CircleShape,
            color = if (!enabled) colors.surfaceVariant else if (outlined) colors.surface else colors.primary,
            border = if (outlined && enabled) BorderStroke(1.dp, colors.primary) else null) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Icon(icon, description, Modifier.size(22.dp), tint = if (!enabled) colors.onSurfaceVariant else if (outlined) colors.primary else colors.onPrimary)
            }
        }
    }
}
@Composable
fun QuantityStepper(quantity: Double, productName: String, onDecrease: () -> Unit, onIncrease: () -> Unit,
    modifier: Modifier = Modifier, canDecrease: Boolean = true, canIncrease: Boolean = true, catalog: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier, shape = RoundedCornerShape(if (catalog) 8.dp else 24.dp),
        color = if (catalog) colors.primaryContainer else colors.surface,
        border = if (catalog) null else BorderStroke(1.dp, colors.outlineVariant)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            RoundQuantityAction(Icons.Default.Remove, "Kurangi $productName", onDecrease, canDecrease, outlined = !catalog)
            Text(quantityLabel(quantity), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp))
            RoundQuantityAction(Icons.Default.Add, "Tambah $productName", onIncrease, canIncrease)
        }
    }
}
