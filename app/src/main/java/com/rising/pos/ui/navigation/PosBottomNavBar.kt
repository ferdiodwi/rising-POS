package com.rising.pos.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.ui.theme.CashierTheme
import com.rising.pos.ui.components.CashierIcons

/** Four phone destinations; remaining screens are accessible through Lainnya. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosBottomNavBar(items: List<Screen>, currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) = CashierTheme {
    var moreOpen by remember { mutableStateOf(false) }
    val direct = items.take(3)
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface) {
        Column(modifier.navigationBarsPadding()) {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth()) {
                (direct.map { it.title } + "Lainnya").forEachIndexed { index, title ->
                    val selected = if (index < 3) currentRoute == direct[index].route else direct.none { it.route == currentRoute }
                    val color = if (selected) colors.primary else colors.onSurfaceVariant
                    Column(Modifier.weight(1f).heightIn(min = 56.dp).selectable(selected, role = Role.Tab,
                        onClick = { if (index == 3) moreOpen = true else onNavigate(direct[index].route) }).padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(when (index) { 0 -> CashierIcons.Store; 1 -> CashierIcons.Box; 2 -> Icons.Outlined.History; else -> Icons.Outlined.Menu }, null, Modifier.size(24.dp), tint = color)
                        Text(title, fontSize = 10.sp, color = color)
                    }
                }
            }
        }
    }
    if (moreOpen) ModalBottomSheet(onDismissRequest = { moreOpen = false }, containerColor = colors.surface) {
        Text("Lainnya", Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge)
        listOf(Screen.Dashboard, Screen.Inventory, Screen.Expenses, Screen.Customers, Screen.Settings).forEach { screen ->
            Surface(onClick = { moreOpen = false; onNavigate(screen.route) }, color = colors.surface) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                    screen.unselectedIcon?.let { Icon(it, null) }; Spacer(Modifier.width(16.dp)); Text(screen.title)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
