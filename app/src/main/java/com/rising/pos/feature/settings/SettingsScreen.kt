package com.rising.pos.feature.settings

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Sync
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
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import com.rising.pos.feature.pos.components.TableFloorDialog
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
import com.rising.pos.ui.theme.PrimaryBlueContainer
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate800
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.widthIn

/**
 * Pengaturan usaha dan perangkat, dikelompokkan berdasarkan tugas kasir.
 * Warna dan kontrol mengikuti tema aplikasi.
 */
@Composable
private fun SettingsScreenContent(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600
    val isCompactHeight = configuration.screenHeightDp < 500
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val syncPendingCount by viewModel.syncPendingCount.collectAsState()

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

    if (isWideScreen) {
        SettingsTabletMasterDetail(
            settings = settings,
            uiState = uiState,
            updateState = updateState,
            hasPrinter = hasPrinter,
            onBackClick = onBackClick,
            onUpdateProfile = { name, phone, address, footer, deviceId, email, logo ->
                viewModel.updateBusinessProfile(name, phone, address, footer, deviceId, email, logo)
            },
            onUpdateTax = { taxEnabled, taxPct, isIncl, svcEnabled, svcPct ->
                viewModel.updateTaxAndService(taxEnabled, taxPct, isIncl, svcEnabled, svcPct)
            },
            onUpdateToggles = { table, modifier, barcode, stock ->
                viewModel.updateFeatureToggles(table, modifier, barcode, stock)
            },
            onSelectPrinter = { viewModel.openPrinterPicker() },
            onTestPrint = { viewModel.testPrint() },
            onSetPaperWidth = { viewModel.setPaperWidth(it) },
            onSetAutoPrint = { viewModel.setAutoPrint(it) },
            onBackupDatabase = { viewModel.backupDatabase(context) },
            onRestoreDatabase = {
                if (settings.isPinSecurityEnabled) {
                    isPinAuthForRestoreOpen = true
                } else {
                    isRestoreConfirmationOpen = true
                }
            },
            onExportCsv = { isExportReportDialogOpen = true },
            onSyncNow = { viewModel.syncNow() },
            onCheckUpdates = { viewModel.checkForUpdates() }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (isCompactHeight) 14.dp else 20.dp,
                        end = if (isCompactHeight) 14.dp else 20.dp,
                        top = if (isCompactHeight) 6.dp else 12.dp,
                        bottom = if (isCompactHeight) 4.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Kembali", tint = Slate900)
                }
                Spacer(Modifier.width(8.dp))
            }
            com.rising.pos.ui.components.PosHeaderTitleSection(
                title = "Pengaturan",
                subtitle = settings.name.ifBlank { "Rising Studio" },
                extraSubtitle = if (isCompactHeight) null else "Sesuaikan usaha dan perangkat kasir."
            )
        }

        // ── Konten Pengaturan ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isWideScreen) 24.dp else 16.dp)
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
                if (settings.isTableEnabled) {
                    SettingsDivider()
                    SettingNavigationItem(
                        title = "Kelola Meja Restoran",
                        subtitle = "Atur nomor meja dan kapasitas (${tables.size} meja)",
                        icon = Icons.Outlined.TableRestaurant,
                        onClick = viewModel::openTableManagement
                    )
                }
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

            // ── Section: Sinkronisasi Cloud (Offline-First) ──────────────────
            SettingsSectionTitle("Sinkronisasi Cloud")
            SettingsGroupCard {
                SettingNavigationItem(
                    title = "Sinkronkan Sekarang",
                    subtitle = if (syncPendingCount > 0) {
                        "$syncPendingCount data antrean menunggu sinkron ke cloud"
                    } else {
                        "Semua data lokal telah tersinkron ke cloud"
                    },
                    icon = Icons.Outlined.Sync,
                    isLoading = uiState.isSyncing,
                    onClick = {
                        if (!uiState.isSyncing) viewModel.syncNow()
                    }
                )
            }

            uiState.syncStatusMessage?.let { msg ->
                val isSuccess = msg.contains("Berhasil", ignoreCase = true) || msg.contains("tersinkron", ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) SuccessGreenContainer else DangerRedContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSuccess) SuccessGreen else DangerRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = viewModel::dismissSyncMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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

    if (uiState.isTableManagementDialogOpen) {
        TableFloorDialog(
            tables = tables,
            heldOrders = emptyList(),
            isSelectionMode = false,
            currencySymbol = settings.currencySymbol,
            onDismiss = viewModel::closeTableManagement,
            onSelectTable = { },
            onSaveTable = viewModel::saveTable,
            onDeleteTable = viewModel::deleteTable,
            onToggleOccupied = viewModel::toggleTableOccupied,
            onSeedDefaultTables = viewModel::seedDefaultTables
        )
    }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

enum class SettingsTabletMenu(val label: String) {
    STORE_INFO("Informasi toko"),
    PAYMENT_METHODS("Metode pembayaran"),
    TAX_DISCOUNT("Pajak & diskon"),
    RECEIPT_PRINTER("Printer struk"),
    BARCODE_SCANNER("Pemindai barcode"),
    BACKUP_DATA("Cadangkan data"),
    HELP("Bantuan"),
    ABOUT_APP("Tentang aplikasi")
}

@Composable
private fun SettingsTabletMasterDetail(
    settings: com.rising.pos.core.datastore.BusinessSettings,
    uiState: SettingsUiState,
    updateState: UpdateState,
    hasPrinter: Boolean,
    onBackClick: (() -> Unit)?,
    onUpdateProfile: (String, String, String, String, String, String, String) -> Unit,
    onUpdateTax: (Boolean, Double, Boolean, Boolean, Double) -> Unit,
    onUpdateToggles: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onSelectPrinter: () -> Unit,
    onTestPrint: () -> Unit,
    onSetPaperWidth: (Int) -> Unit,
    onSetAutoPrint: (Boolean) -> Unit,
    onBackupDatabase: () -> Unit,
    onRestoreDatabase: () -> Unit,
    onExportCsv: () -> Unit,
    onSyncNow: () -> Unit,
    onCheckUpdates: () -> Unit
) {
    var selectedMenu by remember { mutableStateOf(SettingsTabletMenu.STORE_INFO) }

    // Form state untuk STORE_INFO
    var storeName by remember(settings.name) { mutableStateOf(settings.name) }
    var storeAddress by remember(settings.address) { mutableStateOf(settings.address) }
    var storePhone by remember(settings.phone) { mutableStateOf(settings.phone) }
    var storeEmail by remember(settings.email) { mutableStateOf(settings.email) }
    var storeLogo by remember(settings.logoUrl) { mutableStateOf(settings.logoUrl) }
    var logoError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val result = withContext(Dispatchers.IO) { importStoreLogo(context, uri) }
            result.onSuccess { storeLogo = it; logoError = null }
                .onFailure { logoError = it.message ?: "Gagal mengunggah logo." }
        }
    }
    var storeFooter by remember(settings.footerNote) { mutableStateOf(settings.footerNote) }

    // Form state untuk TAX_DISCOUNT
    var isTaxEnabled by remember(settings.isTaxEnabled) { mutableStateOf(settings.isTaxEnabled) }
    var taxPercentageText by remember(settings.taxPercentage) { mutableStateOf(settings.taxPercentage.toString()) }
    var isTaxInclusive by remember(settings.isTaxInclusive) { mutableStateOf(settings.isTaxInclusive) }
    var isServiceChargeEnabled by remember(settings.isServiceChargeEnabled) { mutableStateOf(settings.isServiceChargeEnabled) }
    var serviceChargePercentageText by remember(settings.serviceChargePercentage) { mutableStateOf(settings.serviceChargePercentage.toString()) }

    val storeActions: @Composable () -> Unit = {
        // Tombol Aksi Bawah
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                enabled = !uiState.isSavingProfile,
                onClick = {
                    storeName = settings.name
                    storeAddress = settings.address
                    storePhone = settings.phone
                    storeFooter = settings.footerNote
                    storeEmail = settings.email
                    storeLogo = settings.logoUrl
                    logoError = null
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Slate200),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Batal",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Button(
                enabled = !uiState.isSavingProfile && storeName.isNotBlank() && (storeEmail.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(storeEmail.trim()).matches()),
                onClick = {
                    onUpdateProfile(
                        storeName,
                        storePhone,
                        storeAddress,
                        storeFooter,
                        settings.deviceId,
                        storeEmail.trim(),
                        storeLogo
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (uiState.isSavingProfile) "Menyimpan…" else "Simpan perubahan",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Kembali", tint = Slate900)
                }
                Spacer(Modifier.width(8.dp))
            }
            com.rising.pos.ui.components.PosHeaderTitleSection(
                title = "Pengaturan",
                subtitle = settings.name.ifBlank { "Rising Studio" }
            )
        }

        HorizontalDivider(color = Slate200, thickness = 1.dp)

        // Split Master - Detail
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ── PANEL KIRI: SIDEBAR NAVIGASI (Width: 300.dp) ──
            Column(
                modifier = Modifier
                    .width((LocalConfiguration.current.screenWidthDp * 0.31f).dp.coerceIn(200.dp, 320.dp))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Profile Card Header (Rising Studio / Pengaturan usaha)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = settings.name.ifBlank { "Rising Studio" },
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Pengaturan usaha",
                            fontSize = 12.5.sp,
                            color = Slate500
                        )
                    }
                }

                // ── Group: USAHA ──
                Text(
                    text = "USAHA",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 8.dp)
                )

                SidebarNavItem(
                    title = "Informasi toko",
                    icon = Icons.Outlined.Storefront,
                    isSelected = selectedMenu == SettingsTabletMenu.STORE_INFO,
                    onClick = { selectedMenu = SettingsTabletMenu.STORE_INFO }
                )
                SidebarNavItem(
                    title = "Metode pembayaran",
                    icon = Icons.Outlined.CreditCard,
                    isSelected = selectedMenu == SettingsTabletMenu.PAYMENT_METHODS,
                    onClick = { selectedMenu = SettingsTabletMenu.PAYMENT_METHODS }
                )
                SidebarNavItem(
                    title = "Pajak & diskon",
                    icon = Icons.Outlined.LocalOffer,
                    isSelected = selectedMenu == SettingsTabletMenu.TAX_DISCOUNT,
                    onClick = { selectedMenu = SettingsTabletMenu.TAX_DISCOUNT }
                )

                Spacer(Modifier.height(16.dp))

                // ── Group: PERANGKAT ──
                Text(
                    text = "PERANGKAT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 8.dp)
                )

                SidebarNavItem(
                    title = "Printer struk",
                    icon = Icons.Outlined.Print,
                    isSelected = selectedMenu == SettingsTabletMenu.RECEIPT_PRINTER,
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        if (hasPrinter) Color(0xFF16A34A) else Slate400,
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (hasPrinter) "Dipilih" else "Belum",
                                fontSize = 12.sp,
                                color = if (hasPrinter) Color(0xFF16A34A) else Slate400,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onClick = { selectedMenu = SettingsTabletMenu.RECEIPT_PRINTER }
                )
                SidebarNavItem(
                    title = "Pemindai barcode",
                    icon = Icons.Outlined.QrCodeScanner,
                    isSelected = selectedMenu == SettingsTabletMenu.BARCODE_SCANNER,
                    onClick = { selectedMenu = SettingsTabletMenu.BARCODE_SCANNER }
                )

                Spacer(Modifier.height(16.dp))

                // ── Group: APLIKASI ──
                Text(
                    text = "APLIKASI",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 8.dp)
                )

                SidebarNavItem(
                    title = "Cadangkan data",
                    icon = Icons.Outlined.CloudUpload,
                    isSelected = selectedMenu == SettingsTabletMenu.BACKUP_DATA,
                    onClick = { selectedMenu = SettingsTabletMenu.BACKUP_DATA }
                )
                SidebarNavItem(
                    title = "Bantuan",
                    icon = Icons.Outlined.HelpOutline,
                    isSelected = selectedMenu == SettingsTabletMenu.HELP,
                    onClick = { selectedMenu = SettingsTabletMenu.HELP }
                )
                SidebarNavItem(
                    title = "Tentang aplikasi",
                    icon = Icons.Outlined.Info,
                    isSelected = selectedMenu == SettingsTabletMenu.ABOUT_APP,
                    trailingContent = {
                        Text(
                            text = BuildConfig.VERSION_NAME.ifBlank { "1.0.0" },
                            fontSize = 12.5.sp,
                            color = Slate400
                        )
                    },
                    onClick = { selectedMenu = SettingsTabletMenu.ABOUT_APP }
                )
            }

            // Divider Pemisah Vertikal
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Slate200)
            )

            // ── PANEL KANAN: DETAIL CONTENT FORM ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("settings_detail")) {
                    when (selectedMenu) {
                        SettingsTabletMenu.STORE_INFO -> {
                            // Title & Subtitle
                            Text(
                                text = "Informasi toko",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Identitas usaha yang tampil pada struk.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(24.dp))

                            // Logo Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (storeLogo.isNotBlank()) {
                                        coil.compose.AsyncImage(storeLogo, "Logo toko", Modifier.fillMaxSize().padding(8.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                                    } else Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Spacer(Modifier.width(20.dp))

                                Column {
                                    Text(
                                        text = "Logo toko",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { logoPicker.launch(arrayOf("image/png", "image/jpeg")) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Unggah logo",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "PNG atau JPG, maksimal 2 MB",
                                        fontSize = 11.5.sp,
                                        color = Slate400
                                    )
                                }
                            }

                            logoError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                            Spacer(Modifier.height(24.dp))

                            // Nama toko
                            Text(
                                text = "Nama toko",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                isError = storeName.isBlank(),
                                supportingText = if (storeName.isBlank()) { { Text("Nama toko wajib diisi.") } } else null,
                                placeholder = { Text("Rising Studio", color = Slate400) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(Modifier.height(18.dp))

                            // Alamat toko
                            Text(
                                text = "Alamat toko",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = storeAddress,
                                onValueChange = { storeAddress = it },
                                placeholder = { Text("Tambahkan alamat toko", color = Slate400) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(88.dp),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 3
                            )

                            Spacer(Modifier.height(18.dp))

                            // Nomor telepon & Email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nomor telepon (opsional)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = storePhone,
                                        onValueChange = { storePhone = it },
                                        placeholder = { Text("Tambahkan nomor telepon", color = Slate400) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Email (opsional)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = storeEmail,
                                        onValueChange = { storeEmail = it },
                                        isError = storeEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(storeEmail.trim()).matches(),
                                        supportingText = if (storeEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(storeEmail.trim()).matches()) {
                                            { Text("Masukkan alamat email yang valid.") }
                                        } else null,
                                        placeholder = { Text("Tambahkan email toko", color = Slate400) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // Pesan pada struk
                            Text(
                                text = "Pesan pada struk",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = storeFooter,
                                onValueChange = { storeFooter = it },
                                placeholder = { Text("Terima kasih sudah berbelanja.", color = Slate400) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            Spacer(Modifier.height(14.dp))

                            // Info caption
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Informasi toko akan digunakan pada struk transaksi.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }

                            Spacer(Modifier.height(28.dp))

                        }

                        SettingsTabletMenu.PAYMENT_METHODS -> {
                            Text(
                                text = "Metode pembayaran",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Metode pembayaran yang tersedia di kasir.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            val methods = listOf(
                                Triple("Tunai (Cash)", "Menerima uang tunai dan hitung kembalian otomatis", true),
                                Triple("QRIS", "Menerima pembayaran QRIS statis maupun dinamis", true),
                                Triple("Transfer Bank", "Menerima transfer manual BCA, Mandiri, BRI, BNI", true),
                                Triple("Kartu Debit / EDC", "Menerima pembayaran lewat mesin EDC toko", true)
                            )

                            methods.forEach { (name, desc, isAvailable) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Slate200)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                                            Spacer(Modifier.height(2.dp))
                                            Text(desc, fontSize = 12.5.sp, color = Slate500)
                                        }
                                        Switch(
                                            checked = isAvailable,
                                            onCheckedChange = null,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        SettingsTabletMenu.TAX_DISCOUNT -> {
                            Text(
                                text = "Pajak & diskon",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Atur tarif PPN, PB1, dan biaya layanan restoran.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pajak (PPN / PB1)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                            Text("Aktifkan pajak pada setiap transaksi", fontSize = 12.5.sp, color = Slate500)
                                        }
                                        Switch(
                                            checked = isTaxEnabled,
                                            onCheckedChange = { isTaxEnabled = it },
                                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    if (isTaxEnabled) {
                                        Spacer(Modifier.height(14.dp))
                                        HorizontalDivider(color = Slate100)
                                        Spacer(Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Persentase Pajak (%)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                                Spacer(Modifier.height(6.dp))
                                                OutlinedTextField(
                                                    value = taxPercentageText,
                                                    onValueChange = { taxPercentageText = it },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Tipe Perhitungan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                                Spacer(Modifier.height(6.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = isTaxInclusive,
                                                        onCheckedChange = { isTaxInclusive = it }
                                                    )
                                                    Text("Harga sudah termasuk pajak (Inclusive)", fontSize = 12.5.sp, color = Slate700)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Biaya Layanan (Service Charge)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                            Text("Tambahan servis untuk kafe & restoran", fontSize = 12.5.sp, color = Slate500)
                                        }
                                        Switch(
                                            checked = isServiceChargeEnabled,
                                            onCheckedChange = { isServiceChargeEnabled = it },
                                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    if (isServiceChargeEnabled) {
                                        Spacer(Modifier.height(14.dp))
                                        HorizontalDivider(color = Slate100)
                                        Spacer(Modifier.height(14.dp))

                                        Text("Persentase Biaya Layanan (%)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Spacer(Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = serviceChargePercentageText,
                                            onValueChange = { serviceChargePercentageText = it },
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(0.5f)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = {
                                        onUpdateTax(
                                            isTaxEnabled,
                                            taxPercentageText.toDoubleOrNull() ?: 11.0,
                                            isTaxInclusive,
                                            isServiceChargeEnabled,
                                            serviceChargePercentageText.toDoubleOrNull() ?: 5.0
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Simpan perubahan", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        SettingsTabletMenu.RECEIPT_PRINTER -> {
                            Text(
                                text = "Printer struk",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Koneksikan printer thermal Bluetooth untuk cetak struk kasir.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(if (hasPrinter) Color(0xFFDCFCE7) else Slate100, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Outlined.Print,
                                                contentDescription = null,
                                                tint = if (hasPrinter) Color(0xFF16A34A) else Slate500,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (hasPrinter) settings.printerName.ifBlank { settings.printerMacAddress } else "Belum terhubung",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Slate900
                                            )
                                            Text(
                                                text = if (hasPrinter) "MAC: ${settings.printerMacAddress} · Lebar ${settings.printerPaperWidthMm} mm" else "Pilih printer thermal bluetooth Anda",
                                                fontSize = 12.5.sp,
                                                color = Slate500
                                            )
                                        }
                                        Button(
                                            onClick = onSelectPrinter,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(if (hasPrinter) "Ganti Printer" else "Pilih Printer", color = Color.White, fontSize = 13.sp)
                                        }
                                    }

                                    Spacer(Modifier.height(18.dp))
                                    HorizontalDivider(color = Slate100)
                                    Spacer(Modifier.height(18.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Lebar Kertas", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                            Text("Sesuaikan dengan spesifikasi printer thermal Anda", fontSize = 12.sp, color = Slate500)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(58, 80).forEach { w ->
                                                val isSelected = settings.printerPaperWidthMm == w
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Slate200, RoundedCornerShape(8.dp))
                                                        .clickable { onSetPaperWidth(w) }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "$w mm",
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Slate700
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = Slate100)
                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Cetak Struk Otomatis", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                            Text("Langsung cetak struk begitu transaksi berhasil", fontSize = 12.sp, color = Slate500)
                                        }
                                        Switch(
                                            checked = settings.autoPrintReceipt,
                                            onCheckedChange = { onSetAutoPrint(it) },
                                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = onTestPrint,
                                enabled = hasPrinter && !uiState.isTestingPrint,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isTestingPrint) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mengirim Uji Cetak...", color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Outlined.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Uji Cetak Struk Contoh", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        SettingsTabletMenu.BARCODE_SCANNER -> {
                            Text(
                                text = "Pemindai barcode",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Pengaturan pemindai barcode kamera & hardware USB/Bluetooth.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Aktifkan Barcode Scanner", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                        Spacer(Modifier.height(2.dp))
                                        Text("Memungkinkan scan barcode barang di layar kasir dengan cepat", fontSize = 12.5.sp, color = Slate500)
                                    }
                                    Switch(
                                        checked = settings.isBarcodeEnabled,
                                        onCheckedChange = {
                                            onUpdateToggles(
                                                settings.isTableEnabled,
                                                settings.isModifierEnabled,
                                                it,
                                                settings.isStockTrackingEnabled
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }

                        SettingsTabletMenu.BACKUP_DATA -> {
                            Text(
                                text = "Cadangkan data",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Ekspor data CSV, backup database lokal & sinkronisasi cloud.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text("Database Kasir Lokal", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                    Text("Simpan salinan database ke penyimpanan perangkat atau pulihkan data sebelumnya.", fontSize = 12.5.sp, color = Slate500)

                                    Spacer(Modifier.height(16.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = onBackupDatabase,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Cadangkan Database", color = Color.White)
                                        }
                                        OutlinedButton(
                                            onClick = onRestoreDatabase,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Slate200)
                                        ) {
                                            Text("Pulihkan Database", color = Slate700)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text("Ekspor Laporan & Data CSV", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                    Text("Ekspor data transaksi, produk, dan biaya ke file Excel/CSV.", fontSize = 12.5.sp, color = Slate500)

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = onExportCsv,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Buka Menu Ekspor CSV", color = Color.White)
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text("Sinkronisasi Cloud", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                    Text("Kirim transaksi dan produk terbaru ke server cloud.", fontSize = 12.5.sp, color = Slate500)

                                    Spacer(Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = onSyncNow,
                                        enabled = !uiState.isSyncing,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        if (uiState.isSyncing) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Menyinkronkan...", color = MaterialTheme.colorScheme.primary)
                                        } else {
                                            Icon(Icons.Outlined.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Sinkronkan Sekarang", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        SettingsTabletMenu.HELP -> {
                            Text(
                                text = "Bantuan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Panduan operasional kasir dan dukungan teknis.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            val faqs = listOf(
                                "Bagaimana cara melakukan split payment?" to "Saat pembayaran di layar Kasir, pilih opsi Split Bill untuk membagi total tagihan menjadi dua metode bayar (misal tunai + QRIS).",
                                "Bagaimana cara koneksi printer bluetooth?" to "Buka menu Printer struk, pilih 'Cari Printer', pastikan Bluetooth tablet aktif dan pasangkan dengan printer thermal Anda.",
                                "Apakah kasir tetap bisa digunakan tanpa internet?" to "Ya! Aplikasi Rising POS dirancang offline-first 100%. Semua transaksi dan data produk tersimpan lokal di perangkat."
                            )

                            faqs.forEach { (q, a) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Slate200)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(q, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                                        Spacer(Modifier.height(4.dp))
                                        Text(a, fontSize = 12.5.sp, color = Slate600, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }

                        SettingsTabletMenu.ABOUT_APP -> {
                            Text(
                                text = "Tentang aplikasi",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Informasi versi aplikasi dan lisensi.",
                                fontSize = 13.sp,
                                color = Slate500
                            )

                            Spacer(Modifier.height(20.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Rising POS", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Flexible Offline-First POS Android", fontSize = 13.sp, color = Slate500)
                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = Slate100)
                                    Spacer(Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Versi Aplikasi", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                            Text("Versi ${BuildConfig.VERSION_NAME.ifBlank { "1.0.0" }}", fontSize = 12.5.sp, color = Slate500)
                                        }
                                        Button(
                                            onClick = onCheckUpdates,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Periksa Update", color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (selectedMenu == SettingsTabletMenu.STORE_INFO) {
                    HorizontalDivider(color = Slate200)
                    uiState.profileStatusMessage?.let { message ->
                        Text(message, modifier = Modifier.padding(vertical = 6.dp), fontSize = 12.sp, color = Slate500)
                    }
                    storeActions()
                }
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Slate700

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .drawBehind {
                if (isSelected) drawRect(accent, size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height))
            }
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), onBackClick: (() -> Unit)? = null) {
    if (LocalConfiguration.current.screenWidthDp >= 600) {
        com.rising.pos.ui.theme.TabletCashierTheme { SettingsScreenContent(viewModel, onBackClick) }
    } else SettingsScreenContent(viewModel, onBackClick)
}
