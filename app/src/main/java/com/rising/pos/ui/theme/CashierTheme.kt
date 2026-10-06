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

/** Tablet checkout palette follows the white, blue and navy reference. */
@Composable
fun TabletCashierTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    val colors = if (LocalPosColors.current.isDark) base else base.copy(
        surface = Color.White,
        primary = Color(0xFF006BFF),
        primaryContainer = Color(0xFFEDF5FF),
        onSurface = Color(0xFF091638),
        onSurfaceVariant = Color(0xFF53678B),
        surfaceContainer = Color(0xFFF0F3F6),
        surfaceContainerLow = Color(0xFFF5F7FA),
        outlineVariant = Color(0xFFDCE4EF)
    )
    MaterialTheme(colorScheme = colors, content = content)
}
