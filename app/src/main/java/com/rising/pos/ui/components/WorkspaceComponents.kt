package com.rising.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rising.pos.ui.theme.*

@Composable
fun WorkspaceHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Slate900)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Slate500)
    }
}

@Composable
fun WorkspaceEmptyState(icon: ImageVector, title: String, description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Slate200)) {
            Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.padding(18.dp).size(28.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = Slate900, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = Slate500, textAlign = TextAlign.Center)
    }
}
