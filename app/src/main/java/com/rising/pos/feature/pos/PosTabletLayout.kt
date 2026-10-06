package com.rising.pos.feature.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rising.pos.ui.theme.TabletCashierTheme

/** A full-width toolbar above independently scrolling catalog and cart panes. */
@Composable
internal fun PosTabletLayout(
    header: @Composable () -> Unit,
    catalog: @Composable (columns: Int) -> Unit,
    cart: @Composable () -> Unit
) = TabletCashierTheme {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val shortWindow = maxHeight < 420.dp
            val cartWidth = (maxWidth * 0.39f).coerceIn(280.dp, 560.dp)
            val catalogWidth = maxWidth - cartWidth - 1.dp - 40.dp
            val columns = ((catalogWidth.value + 12f) / 192f).toInt().coerceIn(1, 3)

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = if (shortWindow) 6.dp else 14.dp)) {
                    header()
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 20.dp)) {
                        catalog(columns)
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(Modifier.width(cartWidth).fillMaxHeight()) {
                        cart()
                    }
                }
            }
        }
    }
}
