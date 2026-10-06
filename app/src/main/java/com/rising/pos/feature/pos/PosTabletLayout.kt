package com.rising.pos.feature.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A full-width toolbar above independently scrolling catalog and cart panes. */
@Composable
internal fun PosTabletLayout(
    header: @Composable () -> Unit,
    catalog: @Composable (columns: Int) -> Unit,
    cart: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val cartWidth = (maxWidth * 0.38f).coerceIn(340.dp, 480.dp)
        val catalogWidth = maxWidth - cartWidth - 1.dp - 40.dp
        val columns = ((catalogWidth.value + 12f) / 192f).toInt().coerceIn(2, 4)

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
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
