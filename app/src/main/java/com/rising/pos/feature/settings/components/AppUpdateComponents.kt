package com.rising.pos.feature.settings.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.BuildConfig
import com.rising.pos.core.update.model.UpdateState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun AppUpdateCard(
    updateState: UpdateState,
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Update APK",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Pembaruan Aplikasi",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Versi saat ini: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "GitHub Release",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Periksa dan pasang versi terbaru langsung dari repositori GitHub resmi tanpa perlu install manual dari browser.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
            )

            HorizontalDivider(color = Slate200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isChecking = updateState is UpdateState.Checking

                when (updateState) {
                    is UpdateState.UpToDate -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Aplikasi versi terbaru",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SuccessGreen
                            )
                        }
                    }
                    is UpdateState.UpdateAvailable -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${updateState.latestVersion} tersedia!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningAmber
                            )
                        }
                    }
                    is UpdateState.Error -> {
                        Text(
                            text = "Gagal cek update",
                            fontSize = 12.sp,
                            color = DangerRed
                        )
                    }
                    else -> {
                        Text(
                            text = "Repo: ferdiodwi/rising-POS",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Button(
                    onClick = onCheckUpdate,
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Memeriksa...", fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Periksa Update", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AppUpdateDialog(
    updateState: UpdateState,
    canInstallPackages: Boolean,
    onDownload: (url: String, tag: String) -> Unit,
    onInstall: () -> Unit,
    onOpenUnknownSourcesSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    when (updateState) {
        is UpdateState.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Update Tersedia!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${updateState.currentVersion} ➔ ${updateState.latestVersion}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (updateState.apkSize > 0) {
                            val sizeMb = updateState.apkSize.toDouble() / (1024.0 * 1024.0)
                            Text(
                                text = String.format(Locale.getDefault(), "Ukuran file: %.1f MB", sizeMb),
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        Text(
                            text = "Catatan Rilis:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp),
                            color = Slate200.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = updateState.releaseNotes.ifBlank { "Pembaruan versi ${updateState.latestVersion}" },
                                fontSize = 12.sp,
                                color = Slate700,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDownload(updateState.downloadUrl, updateState.latestVersion)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download & Pasang")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Nanti Saja")
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss while downloading */ },
                icon = {
                    CircularProgressIndicator(
                        progress = { updateState.progress },
                        modifier = Modifier.size(48.dp),
                        color = PrimaryBlue,
                        strokeWidth = 4.dp
                    )
                },
                title = {
                    Text(
                        text = "Mengunduh Update...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { updateState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = PrimaryBlue,
                            trackColor = Slate200
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val percent = (updateState.progress * 100).toInt()
                            Text(
                                text = "$percent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )

                            if (updateState.totalBytes > 0) {
                                val downloadedMb = updateState.bytesDownloaded.toDouble() / (1024.0 * 1024.0)
                                val totalMb = updateState.totalBytes.toDouble() / (1024.0 * 1024.0)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f MB / %.1f MB", downloadedMb, totalMb),
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Text(
                            text = "Harap jangan keluar dari aplikasi selama proses pengunduhan berlangsung.",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.Downloaded -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Unduhan Selesai!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "File APK pembaruan ${updateState.latestVersion} telah berhasil diunduh dan siap dipasang ke perangkat Anda.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                        )

                        if (!canInstallPackages) {
                            Surface(
                                color = WarningAmber.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Perlu izin 'Install aplikasi yang tidak dikenal' di Pengaturan HP untuk memasang update.",
                                        fontSize = 11.sp,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!canInstallPackages) {
                                onOpenUnknownSourcesSettings()
                            } else {
                                onInstall()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text(if (!canInstallPackages) "Beri Izin Install" else "Pasang Sekarang")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Tutup")
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Informasi Update",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = updateState.message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Tutup")
                    }
                }
            )
        }

        is UpdateState.UpToDate -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Versi Terbaru",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "Aplikasi Anda sudah menggunakan versi paling baru (v${updateState.currentVersion}). Tidak ada update yang diperlukan saat ini.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Oke")
                    }
                }
            )
        }

        else -> {
            // Idle or Checking (handled via Card progress state)
        }
    }
}
