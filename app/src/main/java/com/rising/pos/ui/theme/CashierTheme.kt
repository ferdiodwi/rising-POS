package com.rising.pos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CashierTheme(content: @Composable () -> Unit) {
    val dark = LocalPosColors.current.isDark
    val base = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = base.copy(
        primary = if (dark) Color(0xFF8DBBFF) else Color(0xFF1677FF),
        primaryContainer = if (dark) Color(0xFF19365C) else Color(0xFFEDF5FF),
        background = base.surface,
        onSurfaceVariant = if (dark) base.onSurfaceVariant else Color(0xFF818895)
    ), content = content)
}
fun posMoney(amount: Long, symbol: String = "Rp"): String = symbol + groupedAmount(amount)
fun groupedAmount(amount: Long): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(amount)
