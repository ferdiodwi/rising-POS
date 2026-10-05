package com.rising.pos.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.ui.theme.CashierTheme
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.components.CashierIcons

/** Five primary phone destinations: Kasir, Produk, Riwayat, Laporan, Pengaturan. */
@Composable
fun PosBottomNavBar(
    items: List<Screen>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) = CashierTheme {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface) {
        Column(modifier.navigationBarsPadding()) {
            HorizontalDivider(color = colors.outlineVariant, thickness = 0.8.dp)
            Row(Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, screen ->
                    val selected = currentRoute == screen.route
                    val color = if (selected) colors.primary else Slate700
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .selectable(
                                selected,
                                role = Role.Tab,
                                onClick = { onNavigate(screen.route) }
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val icon = when (index) {
                            0 -> CashierIcons.Store
                            1 -> CashierIcons.Box
                            2 -> Icons.Outlined.History
                            3 -> CashierIcons.BarChart
                            else -> Icons.Outlined.Settings
                        }
                        Icon(icon, contentDescription = screen.title, Modifier.size(22.dp), tint = color)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
