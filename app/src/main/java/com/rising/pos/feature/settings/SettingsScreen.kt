package com.rising.pos.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.BuildConfig
import com.rising.pos.core.model.AppTheme
import com.rising.pos.core.update.model.UpdateState
import com.rising.pos.feature.settings.components.AppUpdateDialog
import com.rising.pos.feature.settings.components.PrinterPickerDialog
import com.rising.pos.ui.components.PinSetupDialog
import com.rising.pos.ui.components.SecurityPinDialog
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate800
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer

/**
 * Pengaturan usaha dan perangkat, dikelompokkan berdasarkan tugas kasir.
 * Warna dan kontrol mengikuti tema aplikasi.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    // State dialog modular
    var isStoreProfileDialogOpen by remember { mutableStateOf(false) }
    var isThemeChooserDialogOpen by remember { mutableStateOf(false) }
    var isTaxServiceDialogOpen by remember { mutableStateOf(false) }
    var isPaperWidthDialogOpen by remember { mutableStateOf(false) }
    var isExportReportDialogOpen by remember { mutableStateOf(false) }

    var isRestoreConfirmationOpen by remember { mutableStateOf(false) }
    var isPinAuthForRestoreOpen by remember { mutableStateOf(false) }
    var isPinAuthForSetupOpen by remember { mutableStateOf(false) }

    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    val hasPrinter = settings.printerMacAddress.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Kembali", tint = Slate900)
                }
            }
            Column {
                Text("Pengaturan", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text("Sesuaikan usaha dan perangkat kasir.", style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
        }

        // ── Konten Pengaturan ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Section: General ─────────────────────────────────────────────
            SettingsSectionTitle("Usaha & tampilan")
            SettingsGroupCard {
                SettingNavigationItem(
                    title = "Profil Toko",
                    subtitle = settings.name.ifBlank { "Atur nama toko, kontak & alamat" },
                    icon = Icons.Outlined.Storefront,
                    onClick = { isStoreProfileDialogOpen = true }
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Tema aplikasi",
                    subtitle = when (settings.appTheme) {
                        AppTheme.LIGHT -> "Terang"
                        AppTheme.DARK -> "Gelap"
                        AppTheme.SYSTEM -> "Ikuti Sistem"
                    },
                    icon = Icons.Outlined.LightMode,
                    onClick = { isThemeChooserDialogOpen = true }
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Kode Kasir",
                    subtitle = "Perangkat: ${settings.deviceId}",
                    icon = Icons.Outlined.PhoneAndroid,
                    onClick = { isStoreProfileDialogOpen = true }
                )
            }

            // ── Section: Fitur Kasir ─────────────────────────────────────────
            SettingsSectionTitle("Fitur Kasir")
            SettingsGroupCard {
                SettingSwitchItem(
                    title = "Pencatatan Stok",
                    subtitle = "Otomatis catat stok masuk, keluar & sisa. Bila nonaktif, penjualan tidak mengurangi stok.",
                    icon = Icons.Outlined.Inventory2,
                    checked = settings.isStockTrackingEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateFeatureToggles(
                            settings.isTableEnabled,
                            settings.isModifierEnabled,
                            settings.isBarcodeEnabled,
                            checked
                        )
                    }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Nomor Meja",
                    subtitle = "Dine-in untuk kafe & restoran",
                    icon = Icons.Outlined.TableRestaurant,
                    checked = settings.isTableEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateFeatureToggles(
                            checked,
                            settings.isModifierEnabled,
                            settings.isBarcodeEnabled,
                            settings.isStockTrackingEnabled
                        )
                    }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Topping & Modifier",
                    subtitle = "Opsi tambahan (gula, shot, varian). Bila nonaktif, produk bervarian masuk keranjang dengan harga dasar.",
                    icon = Icons.Outlined.Bolt,
                    checked = settings.isModifierEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateFeatureToggles(
                            settings.isTableEnabled,
                            checked,
                            settings.isBarcodeEnabled,
                            settings.isStockTrackingEnabled
                        )
                    }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Barcode Scanner",
                    subtitle = "Scan barcode fisik & kamera. Bila nonaktif, tombol scan disembunyikan.",
                    icon = Icons.Outlined.QrCodeScanner,
                    checked = settings.isBarcodeEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateFeatureToggles(
                            settings.isTableEnabled,
                            settings.isModifierEnabled,
                            checked,
                            settings.isStockTrackingEnabled
                        )
                    }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Pajak (PPN / PB1)",
                    subtitle = if (settings.isTaxEnabled) {
                        "${settings.taxPercentage}% · ${if (settings.isTaxInclusive) "Sudah Termasuk" else "Ditambahkan"}"
                    } else {
                        "Nonaktif"
                    },
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    checked = settings.isTaxEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateTaxAndService(
                            checked,
                            settings.taxPercentage,
                            settings.isTaxInclusive,
                            settings.isServiceChargeEnabled,
                            settings.serviceChargePercentage
                        )
                    },
                    onClick = { isTaxServiceDialogOpen = true }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Biaya Layanan",
                    subtitle = if (settings.isServiceChargeEnabled) {
                        "${settings.serviceChargePercentage}% dari subtotal"
                    } else {
                        "Nonaktif"
                    },
                    icon = Icons.Outlined.CreditCard,
                    checked = settings.isServiceChargeEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateTaxAndService(
                            settings.isTaxEnabled,
                            settings.taxPercentage,
                            settings.isTaxInclusive,
                            checked,
                            settings.serviceChargePercentage
                        )
                    },
                    onClick = { isTaxServiceDialogOpen = true }
                )
            }

            // ── Section: Perangkat & Printer ─────────────────────────────────
            SettingsSectionTitle("Perangkat & Printer")
            SettingsGroupCard {
                SettingNavigationItem(
                    title = "Printer Struk",
                    subtitle = if (hasPrinter) "${settings.printerName} (${settings.printerMacAddress})" else "Belum ada printer dipilih",
                    icon = Icons.Outlined.Print,
                    valueBadge = if (hasPrinter) "Terhubung" else "Pilih",
                    badgeColor = if (hasPrinter) SuccessGreen else Slate500,
                    onClick = viewModel::openPrinterPicker
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Ukuran Kertas Struk",
                    subtitle = "${settings.printerPaperWidthMm} mm",
                    icon = Icons.Outlined.Description,
                    valueBadge = "${settings.printerPaperWidthMm} mm",
                    onClick = { isPaperWidthDialogOpen = true }
                )
                SettingsDivider()
                SettingSwitchItem(
                    title = "Cetak Otomatis",
                    subtitle = "Langsung cetak struk setelah transaksi berhasil",
                    icon = Icons.Outlined.Print,
                    checked = settings.autoPrintReceipt,
                    onCheckedChange = viewModel::setAutoPrint
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Tes Cetak Struk",
                    subtitle = if (hasPrinter) "Kirim struk uji coba ke printer" else "Pilih printer terlebih dahulu",
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    isLoading = uiState.isTestingPrint,
                    onClick = {
                        if (hasPrinter && !uiState.isTestingPrint) {
                            viewModel.testPrint()
                        }
                    }
                )
            }

            // Feedback status printer jika ada pesan
            uiState.printerStatusMessage?.let { msg ->
                val success = msg.contains("berhasil", ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (success) SuccessGreenContainer else DangerRedContainer
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (success) SuccessGreen else DangerRed,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── Section: Security ────────────────────────────────────────────
            SettingsSectionTitle("Keamanan")
            SettingsGroupCard {
                SettingNavigationItem(
                    title = "PIN Owner",
                    subtitle = if (settings.isPinSecurityEnabled) {
                        "Aktif (Otorisasi pembatalan & pemulihan)"
                    } else {
                        "Nonaktif (Kasir dapat void tanpa PIN)"
                    },
                    icon = Icons.Outlined.Lock,
                    valueBadge = if (settings.isPinSecurityEnabled) "Aktif" else "Nonaktif",
                    badgeColor = if (settings.isPinSecurityEnabled) SuccessGreen else Slate500,
                    onClick = {
                        if (settings.isPinSecurityEnabled && settings.hasPin) {
                            isPinAuthForSetupOpen = true
                        } else {
                            viewModel.openPinSetupDialog()
                        }
                    }
                )
            }

            // ── Section: Data & Cadangan ─────────────────────────────────────
            SettingsSectionTitle("Data & Cadangan")
            SettingsGroupCard {
                val busy = uiState.isExporting || uiState.isRestoring
                SettingNavigationItem(
                    title = "Cadangkan Database",
                    subtitle = "Simpan salinan database (.db) ke perangkat",
                    icon = Icons.Outlined.CloudDownload,
                    isLoading = uiState.isExporting,
                    onClick = {
                        if (!busy) viewModel.backupDatabase(context)
                    }
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Pulihkan Database",
                    subtitle = "Ganti seluruh data dari file cadangan",
                    icon = Icons.Outlined.Restore,
                    isLoading = uiState.isRestoring,
                    onClick = {
                        if (!busy) {
                            if (settings.isPinSecurityEnabled && settings.hasPin) {
                                isPinAuthForRestoreOpen = true
                            } else {
                                isRestoreConfirmationOpen = true
                            }
                        }
                    }
                )
                SettingsDivider()
                SettingNavigationItem(
                    title = "Ekspor Laporan (CSV)",
                    subtitle = "Unduh CSV transaksi, produk, biaya & pelanggan",
                    icon = Icons.Outlined.CloudUpload,
                    onClick = { isExportReportDialogOpen = true }
                )
            }

            // Feedback status pemulihan / cadangan jika ada pesan
            uiState.restoreStatusMessage?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isRestoreSuccess) SuccessGreenContainer else DangerRedContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isRestoreSuccess) SuccessGreen else DangerRed
                        )
                        TextButton(onClick = viewModel::clearRestoreStatus) {
                            Text("Tutup", style = MaterialTheme.typography.labelMedium, color = Slate700)
                        }
                    }
                }
            }

            // ── Section: Sistem & Pembaruan ──────────────────────────────────
            SettingsSectionTitle("Sistem & Pembaruan")
            SettingsGroupCard {
                val isForce = when (val state = updateState) {
                    is UpdateState.UpdateAvailable -> state.isForceUpdate
                    is UpdateState.Downloading -> state.isForceUpdate
                    is UpdateState.Downloaded -> state.isForceUpdate
                    is UpdateState.Error -> state.isForceUpdate
                    else -> false
                }

                val updateSubtitle = when (val state = updateState) {
                    is UpdateState.Checking -> "Sedang memeriksa update..."
                    is UpdateState.UpdateAvailable -> {
                        if (state.isForceUpdate) "Pembaruan wajib (${state.latestVersion})" else "Versi ${state.latestVersion} tersedia"
                    }
                    is UpdateState.UpToDate -> "Aplikasi sudah versi terbaru"
                    is UpdateState.Downloading -> "Mengunduh APK... ${(state.progress * 100).toInt()}%"
                    is UpdateState.Downloaded -> "Pembaruan siap dipasang"
                    is UpdateState.Error -> state.message
                    else -> "Versi saat ini v${BuildConfig.VERSION_NAME} (Kode ${BuildConfig.VERSION_CODE})"
                }

                SettingNavigationItem(
                    title = "Pembaruan Aplikasi",
                    subtitle = updateSubtitle,
                    icon = Icons.Outlined.NewReleases,
                    valueBadge = if (isForce) "Wajib Update" else "v${BuildConfig.VERSION_NAME}",
                    badgeColor = if (isForce) DangerRed else PrimaryBlue,
                    isLoading = updateState is UpdateState.Checking,
                    onClick = viewModel::checkForUpdates
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // ── Dialog: Profil Toko ──────────────────────────────────────────────────
    if (isStoreProfileDialogOpen) {
        StoreProfileDialog(
            initialName = settings.name,
            initialPhone = settings.phone,
            initialAddress = settings.address,
            initialFooterNote = settings.footerNote,
            initialDeviceId = settings.deviceId,
            onDismiss = { isStoreProfileDialogOpen = false },
            onSave = { name, phone, address, footer, deviceId ->
                viewModel.updateBusinessProfile(name, phone, address, footer, deviceId)
                isStoreProfileDialogOpen = false
            }
        )
    }

    // ── Dialog: Pemilihan Tema ───────────────────────────────────────────────
    if (isThemeChooserDialogOpen) {
        ThemeChooserDialog(
            currentTheme = settings.appTheme,
            onDismiss = { isThemeChooserDialogOpen = false },
            onSelectTheme = { theme ->
                viewModel.setAppTheme(theme)
                isThemeChooserDialogOpen = false
            }
        )
    }

    // ── Dialog: Pajak & Biaya Layanan ────────────────────────────────────────
    if (isTaxServiceDialogOpen) {
        TaxAndServiceDialog(
            taxPercentage = settings.taxPercentage,
            isTaxInclusive = settings.isTaxInclusive,
            serviceChargePercentage = settings.serviceChargePercentage,
            onDismiss = { isTaxServiceDialogOpen = false },
            onSave = { taxPct, isIncl, servicePct ->
                viewModel.updateTaxAndService(
                    settings.isTaxEnabled,
                    taxPct,
                    isIncl,
                    settings.isServiceChargeEnabled,
                    servicePct
                )
                isTaxServiceDialogOpen = false
            }
        )
    }

    // ── Dialog: Ukuran Kertas Struk ──────────────────────────────────────────
    if (isPaperWidthDialogOpen) {
        PaperWidthDialog(
            currentWidthMm = settings.printerPaperWidthMm,
            onDismiss = { isPaperWidthDialogOpen = false },
            onSelectWidth = { width ->
                viewModel.setPaperWidth(width)
                isPaperWidthDialogOpen = false
            }
        )
    }

    // ── Dialog: Ekspor Laporan CSV ───────────────────────────────────────────
    if (isExportReportDialogOpen) {
        ExportReportDialog(
            isExporting = uiState.isExporting,
            exportStatusMessage = uiState.exportStatusMessage,
            onDismiss = { isExportReportDialogOpen = false },
            onExportTransactions = { viewModel.exportTransactions(context) },
            onExportProducts = { viewModel.exportProducts(context) },
            onExportExpenses = { viewModel.exportExpenses(context) },
            onExportCustomers = { viewModel.exportCustomers(context) }
        )
    }

    // ── Dialog: Pemilihan Printer ────────────────────────────────────────────
    if (uiState.isPrinterPickerOpen) {
        PrinterPickerDialog(
            printers = uiState.pairedPrinters,
            currentSelectedMac = settings.printerMacAddress,
            onDismiss = viewModel::closePrinterPicker,
            onSelectPrinter = viewModel::selectPrinter
        )
    }

    // ── Dialog: Konfigurasi PIN ──────────────────────────────────────────────
    if (uiState.isPinSetupDialogOpen) {
        PinSetupDialog(
            initialEnabled = settings.isPinSecurityEnabled,
            onDismiss = viewModel::closePinSetupDialog,
            onSavePin = viewModel::updatePinSecurity
        )
    }

    // ── Dialog: Otorisasi PIN untuk Setup ─────────────────────────────────────
    if (isPinAuthForSetupOpen) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi Ubah PIN",
            description = "Masukkan PIN owner saat ini untuk mengubah atau menonaktifkan PIN.",
            onDismiss = { isPinAuthForSetupOpen = false },
            onSuccess = {
                isPinAuthForSetupOpen = false
                viewModel.openPinSetupDialog()
            }
        )
    }

    // ── Dialog: Otorisasi PIN untuk Restore ───────────────────────────────────
    if (isPinAuthForRestoreOpen) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi Pulihkan Database",
            description = "Masukkan PIN owner untuk melanjutkan proses pemulihan database.",
            onDismiss = { isPinAuthForRestoreOpen = false },
            onSuccess = {
                isPinAuthForRestoreOpen = false
                isRestoreConfirmationOpen = true
            }
        )
    }

    // ── Dialog: Konfirmasi Restore ───────────────────────────────────────────
    if (isRestoreConfirmationOpen) {
        AlertDialog(
            onDismissRequest = { isRestoreConfirmationOpen = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Pulihkan Database?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
            },
            text = {
                Text(
                    "Seluruh data saat ini (transaksi, produk, pelanggan, biaya) akan diganti dengan data dari file cadangan yang Anda pilih. Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoreConfirmationOpen = false
                        restoreFilePickerLauncher.launch("*/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pilih File Cadangan", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isRestoreConfirmationOpen = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        )
    }

    // ── Dialog: Pembaruan Aplikasi GitHub ─────────────────────────────────────
    AppUpdateDialog(
        updateState = updateState,
        canInstallPackages = viewModel.canInstallPackages(),
        onDownload = { url, tag, isForce -> viewModel.downloadUpdate(url, tag, isForce) },
        onInstall = viewModel::installUpdate,
        onOpenUnknownSourcesSettings = {
            viewModel.getUnknownSourcesSettingsIntent()?.let { intent ->
                context.startActivity(intent)
            }
        },
        onDismiss = viewModel::dismissUpdate
    )
}

// ── Komponen UI Modern (Mirip Referensi Gambar) ──────────────────────────────

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        color = Slate800,
        modifier = Modifier.padding(start = 4.dp, top = 22.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = Slate200.copy(alpha = 0.4f),
        thickness = 0.8.dp
    )
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() ?: onCheckedChange(!checked) },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate700,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Slate900
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = Slate200
                )
            )
        }
    }
}

@Composable
private fun SettingNavigationItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    valueBadge: String? = null,
    badgeColor: Color = Slate500,
    showChevron: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate700,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Slate900
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Slate500
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryBlue
                )
            } else {
                if (!valueBadge.isNullOrBlank()) {
                    Text(
                        text = valueBadge,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = badgeColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (showChevron) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Dialog Modular Pendukung ─────────────────────────────────────────────────

@Composable
private fun StoreProfileDialog(
    initialName: String,
    initialPhone: String,
    initialAddress: String,
    initialFooterNote: String,
    initialDeviceId: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }
    var footer by remember { mutableStateOf(initialFooterNote) }
    var deviceId by remember { mutableStateOf(initialDeviceId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Profil Usaha & Struk",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Toko") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Telepon / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Toko") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = { Text("Catatan Bawah Struk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("Kode Perangkat Kasir") },
                    supportingText = { Text("Contoh: A01 untuk Kasir 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, address, footer, deviceId) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Text("Batal", color = Slate700)
            }
        }
    )
}

@Composable
private fun ThemeChooserDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onSelectTheme: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Pilih Tampilan (Appearance)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOptionRow(
                    title = "Terang (Light Mode)",
                    icon = Icons.Outlined.LightMode,
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { onSelectTheme(AppTheme.LIGHT) }
                )
                ThemeOptionRow(
                    title = "Gelap (Dark Mode OLED)",
                    icon = Icons.Outlined.DarkMode,
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { onSelectTheme(AppTheme.DARK) }
                )
                ThemeOptionRow(
                    title = "Ikuti Sistem Android",
                    icon = Icons.Outlined.PhoneAndroid,
                    selected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onSelectTheme(AppTheme.SYSTEM) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Slate700)
            }
        }
    )
}

@Composable
private fun ThemeOptionRow(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryBlue.copy(alpha = 0.08f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) PrimaryBlue else Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PrimaryBlue else Slate700,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) PrimaryBlue else Slate900,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TaxAndServiceDialog(
    taxPercentage: Double,
    isTaxInclusive: Boolean,
    serviceChargePercentage: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Boolean, Double) -> Unit
) {
    var taxStr by remember { mutableStateOf(taxPercentage.toString()) }
    var isInclusive by remember { mutableStateOf(isTaxInclusive) }
    var serviceStr by remember { mutableStateOf(serviceChargePercentage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Pajak & Biaya Layanan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = taxStr,
                    onValueChange = { taxStr = it },
                    label = { Text("Tarif Pajak (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isInclusive = !isInclusive }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isInclusive,
                        onCheckedChange = { isInclusive = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Harga produk sudah termasuk pajak (Inclusive)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }

                HorizontalDivider(color = Slate200)

                OutlinedTextField(
                    value = serviceStr,
                    onValueChange = { serviceStr = it },
                    label = { Text("Tarif Biaya Layanan (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTax = taxStr.toDoubleOrNull() ?: taxPercentage
                    val finalService = serviceStr.toDoubleOrNull() ?: serviceChargePercentage
                    onSave(finalTax, isInclusive, finalService)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Text("Batal", color = Slate700)
            }
        }
    )
}

@Composable
private fun PaperWidthDialog(
    currentWidthMm: Int,
    onDismiss: () -> Unit,
    onSelectWidth: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Ukuran Kertas Printer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaperWidthOptionRow(
                    label = "58 mm (Printer Kasir Mini / Standar)",
                    selected = currentWidthMm == 58,
                    onClick = { onSelectWidth(58) }
                )
                PaperWidthOptionRow(
                    label = "80 mm (Printer Kasir Lebar / Desktop)",
                    selected = currentWidthMm == 80,
                    onClick = { onSelectWidth(80) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Slate700)
            }
        }
    )
}

@Composable
private fun PaperWidthOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryBlue.copy(alpha = 0.08f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) PrimaryBlue else Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) PrimaryBlue else Slate900,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportReportDialog(
    isExporting: Boolean,
    exportStatusMessage: String?,
    onDismiss: () -> Unit,
    onExportTransactions: () -> Unit,
    onExportProducts: () -> Unit,
    onExportExpenses: () -> Unit,
    onExportCustomers: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Ekspor Laporan Spreadsheet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih data usaha yang ingin diunduh dalam format .CSV:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(4.dp))

                ExportActionItem(
                    title = "Ekspor Transaksi Penjualan",
                    subtitle = "Riwayat kasir & struk pembayaran",
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    enabled = !isExporting,
                    onClick = onExportTransactions
                )
                ExportActionItem(
                    title = "Ekspor Produk & Menu",
                    subtitle = "Daftar barang, stok & harga jual",
                    icon = Icons.Outlined.Inventory2,
                    enabled = !isExporting,
                    onClick = onExportProducts
                )
                ExportActionItem(
                    title = "Ekspor Pengeluaran & Biaya",
                    subtitle = "Catatan operasional toko",
                    icon = Icons.Outlined.CreditCard,
                    enabled = !isExporting,
                    onClick = onExportExpenses
                )
                ExportActionItem(
                    title = "Ekspor Daftar Pelanggan",
                    subtitle = "Kontak & riwayat belanja",
                    icon = Icons.Outlined.People,
                    enabled = !isExporting,
                    onClick = onExportCustomers
                )

                if (isExporting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang menyiapkan file CSV...", style = MaterialTheme.typography.bodySmall, color = Slate700)
                    }
                }

                exportStatusMessage?.let { msg ->
                    val success = msg.contains("berhasil", ignoreCase = true)
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (success) SuccessGreen else DangerRed,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Slate700)
            }
        }
    )
}

@Composable
private fun ExportActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Slate900)
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp), color = Slate500)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
        }
    }
}
