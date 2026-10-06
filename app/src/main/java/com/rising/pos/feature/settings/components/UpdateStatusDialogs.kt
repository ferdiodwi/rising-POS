package com.rising.pos.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rising.pos.core.update.model.UpdateState

private val UpdateBlue = Color(0xFF006BFF)
private val UpdateGreen = Color(0xFF008C4A)

@Composable
internal fun AppUpToDateDialog(currentVersion: String, onDismiss: () -> Unit) {
    UpdateStatusDialog(
        title = "Aplikasi sudah terbaru",
        subtitle = "Tidak ada pembaruan yang perlu diunduh saat ini.",
        icon = Icons.Default.Check,
        success = true,
        onDismiss = onDismiss,
        actions = { UpdatePrimaryButton("Mengerti", onDismiss) }
    ) {
        VersionPanel {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Versi saat ini", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                VersionNumber(currentVersion)
            }
        }
    }
}

@Composable
internal fun UpdateAvailableDialog(state: UpdateState.UpdateAvailable, onDownload: () -> Unit, onDismiss: () -> Unit) {
    UpdateStatusDialog(
        title = if (state.isForceUpdate) "Pembaruan wajib" else "Pembaruan tersedia",
        subtitle = if (state.isForceUpdate) "Perbarui Rising POS untuk melanjutkan penggunaan." else "Versi baru Rising POS siap diunduh.",
        icon = Icons.Default.Download,
        dismissible = !state.isForceUpdate,
        onDismiss = onDismiss,
        actions = {
            UpdatePrimaryButton("Perbarui sekarang", onDownload)
            if (!state.isForceUpdate) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = UpdateBlue)) {
                    Text("Nanti", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) {
        VersionPanel {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Terpasang", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp)); VersionNumber(state.currentVersion)
                }
                VerticalDivider(Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(horizontal = 12.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                VerticalDivider(Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Versi baru", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp)); VersionNumber(state.latestVersion, UpdateBlue)
                }
            }
        }
        if (state.releaseNotes.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Text("Yang baru", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            state.releaseNotes.trim().lines().forEach { line ->
                val trimmed = line.trim()
                val isBullet = trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    if (isBullet) Text("•", Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isBullet) trimmed.drop(2) else trimmed, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun UpdateStatusDialog(
    title: String, subtitle: String, icon: ImageVector,
    onDismiss: () -> Unit, success: Boolean = false, dismissible: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val width = (configuration.screenWidthDp * .78f).dp.coerceAtMost(420.dp)
    val maxHeight = (configuration.screenHeightDp - 48).coerceAtLeast(160).dp
    val compact = configuration.screenHeightDp < 500
    Dialog(onDismissRequest = { if (dismissible) onDismiss() }, properties = DialogProperties(
        dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible, usePlatformDefaultWidth = false
    )) {
        Surface(Modifier.width(width).heightIn(max = maxHeight), shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            Column(Modifier.padding(20.dp)) {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    val accent = if (success) UpdateGreen else UpdateBlue
                    Box(Modifier.size(if (compact) 48.dp else 64.dp).background(accent.copy(alpha = .10f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        if (success) {
                            Box(Modifier.size(34.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, null, Modifier.size(24.dp), tint = Color.White)
                            }
                        } else Icon(icon, null, Modifier.size(if (compact) 28.dp else 36.dp), tint = accent)
                    }
                    Spacer(Modifier.height(if (compact) 12.dp else 18.dp))
                    Text(title, Modifier.fillMaxWidth(), fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, lineHeight = 26.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(subtitle, Modifier.fillMaxWidth(), fontSize = 14.sp, lineHeight = 21.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(18.dp))
                    Column(Modifier.fillMaxWidth(), content = content)
                }
                Spacer(Modifier.height(18.dp))
                actions()
            }
        }
    }
}

@Composable
private fun VersionPanel(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) { content() }
    }
}

@Composable
private fun VersionNumber(version: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text("v${version.trim().removePrefix("v").removePrefix("V")}", fontSize = 20.sp,
        fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun UpdatePrimaryButton(label: String, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UpdateBlue, contentColor = Color.White)) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
