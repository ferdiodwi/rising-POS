package com.rising.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TabletWorkspaceSplit(
    modifier: Modifier = Modifier,
    master: @Composable ColumnScope.() -> Unit,
    detail: @Composable ColumnScope.() -> Unit
) {
    Row(modifier.fillMaxSize()) {
        Column(Modifier.weight(1.6f).fillMaxHeight().padding(horizontal = 20.dp, vertical = 12.dp), content = master)
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 20.dp, vertical = 12.dp), content = detail)
    }
}

@Composable
internal fun WorkspaceBadge(text: String, color: Color, background: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = background) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color,
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
internal fun WorkspaceDetailField(label: String, value: String, prominent: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Text(label, Modifier.weight(1f), fontSize = if (prominent) 18.sp else 14.sp,
            color = if (prominent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (prominent) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.width(8.dp))
        Text(value, Modifier.weight(1.1f), fontSize = if (prominent) 24.sp else 14.sp,
            fontWeight = if (prominent) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
internal fun WorkspaceAction(label: String, icon: ImageVector, onClick: () -> Unit, secondary: Boolean = false, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    if (secondary) {
        OutlinedButton(onClick, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = enabled,
            shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, colors.primary)) {
            Icon(icon, null, Modifier.size(22.dp)); Spacer(Modifier.width(8.dp))
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(onClick, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(10.dp)) {
            Icon(icon, null, Modifier.size(22.dp)); Spacer(Modifier.width(8.dp))
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun WorkspacePagination(count: Int, page: Int, pageSize: Int, noun: String, onPage: (Int) -> Unit) {
    val pages = ((count + pageSize - 1) / pageSize).coerceAtLeast(1)
    val first = if (count == 0) 0 else page * pageSize + 1
    val last = ((page + 1) * pageSize).coerceAtMost(count)
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Menampilkan $first–$last dari $count $noun", Modifier.weight(1f),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = { onPage(page - 1) }, enabled = page > 0) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Halaman sebelumnya")
        }
        Text("${page + 1} / $pages", fontSize = 13.sp)
        IconButton(onClick = { onPage(page + 1) }, enabled = page < pages - 1) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Halaman berikutnya")
        }
    }
}
