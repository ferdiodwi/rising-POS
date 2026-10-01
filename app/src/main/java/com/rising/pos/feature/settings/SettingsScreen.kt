package com.rising.pos.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.model.AppTheme
import com.rising.pos.feature.settings.components.AppUpdateCard
import com.rising.pos.feature.settings.components.AppUpdateDialog
import com.rising.pos.feature.settings.components.PrinterPickerDialog
import com.rising.pos.ui.components.PinSetupDialog
import com.rising.pos.ui.components.SecurityPinDialog
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer

/**
 * Pengaturan.
 *
 * Halaman ini banyak isi, jadi dipecah menjadi empat bagian lewat segmented
 * control: Toko, Transaksi, Perangkat, Data dan akses. Setiap item punya ikon,
 * judul, dan penjelasan singkat agar tidak terasa menakutkan.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember(settings.name) { mutableStateOf(settings.name) }
    var phone by remember(settings.phone) { mutableStateOf(settings.phone) }
    var address by remember(settings.address) { mutableStateOf(settings.address) }
    var footerNote by remember(settings.footerNote) { mutableStateOf(settings.footerNote) }
    var deviceId by remember(settings.deviceId) { mutableStateOf(settings.deviceId) }

    var isRestoreConfirmationOpen by remember { mutableStateOf(false) }
    var isPinAuthForRestoreOpen by remember { mutableStateOf(false) }
    // Gerbang verifikasi PIN lama sebelum mengubah/menonaktifkan PIN.
    var isPinAuthForSetupOpen by remember { mutableStateOf(false) }

    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    var selectedSection by rememberSaveable { mutableStateOf(0) }
    val sectionScroll = rememberScrollState()
    LaunchedEffect(selectedSection) { sectionScroll.scrollTo(0) }

    val sectionTitles = listOf("Toko", "Transaksi", "Perangkat", "Data dan akses")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Pengaturan", style = MaterialTheme.typography.headlineSmall, color = Slate900)
            Text(
                "Sesuaikan toko, struk, dan akses aplikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        Spacer(Modifier.height(14.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sectionTitles.size) { index ->
                FilterChip(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    label = { Text(sectionTitles[index]) },
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedSection == index) MaterialTheme.colorScheme.primary else Slate200
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = Slate500,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(sectionScroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSection) {
                0 -> StoreSection(
                    name = name,
                    onNameChange = { name = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    address = address,
                    onAddressChange = { address = it },
                    footerNote = footerNote,
                    onFooterNoteChange = { footerNote = it },
                    deviceId = deviceId,
                    onDeviceIdChange = { deviceId = it },
                    appTheme = settings.appTheme,
                    onThemeChange = viewModel::setAppTheme,
                    onSaveProfile = {
                        viewModel.updateBusinessProfile(name, phone, address, footerNote, deviceId)
                    }
                )

                1 -> TransactionSection(
                    settings = settings,
                    onUpdateFeatureToggles = viewModel::updateFeatureToggles,
                    onUpdateTaxAndService = viewModel::updateTaxAndService
                )

                2 -> DeviceSection(
                    settings = settings,
                    isTestingPrint = uiState.isTestingPrint,
                    printerStatusMessage = uiState.printerStatusMessage,
                    onOpenPrinterPicker = viewModel::openPrinterPicker,
                    onSetPaperWidth = viewModel::setPaperWidth,
                    onSetAutoPrint = viewModel::setAutoPrint,
                    onTestPrint = viewModel::testPrint
                )

                3 -> DataSection(
                    settings = settings,
                    uiState = uiState,
                    updateState = updateState,
                    context = context,
                    viewModel = viewModel,
                    onOpenPinSetup = {
                        if (settings.isPinSecurityEnabled && settings.hasPin) {
                            isPinAuthForSetupOpen = true
                        } else {
                            viewModel.openPinSetupDialog()
                        }
                    },
                    onRestoreClick = {
                        if (settings.isPinSecurityEnabled && settings.hasPin) {
                            isPinAuthForRestoreOpen = true
                        } else {
                            isRestoreConfirmationOpen = true
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (uiState.isPrinterPickerOpen) {
        PrinterPickerDialog(
            printers = uiState.pairedPrinters,
            currentSelectedMac = settings.printerMacAddress,
            onDismiss = viewModel::closePrinterPicker,
            onSelectPrinter = viewModel::selectPrinter
        )
    }

    if (uiState.isPinSetupDialogOpen) {
        PinSetupDialog(
            initialEnabled = settings.isPinSecurityEnabled,
            onDismiss = viewModel::closePinSetupDialog,
            onSavePin = viewModel::updatePinSecurity
        )
    }

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

    if (isPinAuthForSetupOpen) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi ubah PIN",
            description = "Masukkan PIN owner saat ini untuk mengubah atau menonaktifkan PIN.",
            onDismiss = { isPinAuthForSetupOpen = false },
            onSuccess = {
                isPinAuthForSetupOpen = false
                viewModel.openPinSetupDialog()
            }
        )
    }

    if (isPinAuthForRestoreOpen) {
        SecurityPinDialog(
            verifyPin = viewModel::verifyPin,
            title = "Otorisasi pulihkan database",
            description = "Masukkan PIN owner untuk melanjutkan proses pemulihan database.",
            onDismiss = { isPinAuthForRestoreOpen = false },
            onSuccess = {
                isPinAuthForRestoreOpen = false
                isRestoreConfirmationOpen = true
            }
        )
    }

    if (isRestoreConfirmationOpen) {
        AlertDialog(
            onDismissRequest = { isRestoreConfirmationOpen = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Pulihkan database?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900
                )
            },
            text = {
                Text(
                    "Seluruh data saat ini (transaksi, produk, pelanggan, biaya) akan diganti dengan data dari file cadangan yang Anda pilih. Tindakan ini tidak bisa dibatalkan.",
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
                    Text("Pilih file cadangan", style = MaterialTheme.typography.labelLarge, color = Color.White)
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
}

// ── Bagian 1: Toko ──────────────────────────────────────────────────────────

@Composable
private fun StoreSection(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    footerNote: String,
    onFooterNoteChange: (String) -> Unit,
    deviceId: String,
    onDeviceIdChange: (String) -> Unit,
    appTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onSaveProfile: () -> Unit
) {
    SettingsCard(title = "Profil usaha", icon = Icons.Outlined.Restaurant) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nama toko") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Nomor telepon / WhatsApp") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Alamat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = footerNote,
            onValueChange = onFooterNoteChange,
            label = { Text("Pesan di bagian bawah struk") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = deviceId,
            onValueChange = onDeviceIdChange,
            label = { Text("Kode perangkat pada struk") },
            supportingText = { Text("Contoh: A01 untuk kasir 1, B01 untuk kasir 2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Button(
            onClick = onSaveProfile,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Simpan profil", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }

    SettingsCard(title = "Tema", icon = Icons.Outlined.Tune) {
        Text(
            "Pilih tampilan yang nyaman untuk tempat kerja Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("Terang", Icons.Outlined.LightMode, appTheme == AppTheme.LIGHT) {
                onThemeChange(AppTheme.LIGHT)
            }
            ThemeChip("Gelap", Icons.Outlined.DarkMode, appTheme == AppTheme.DARK) {
                onThemeChange(AppTheme.DARK)
            }
            ThemeChip("Sistem", Icons.Outlined.PhoneAndroid, appTheme == AppTheme.SYSTEM) {
                onThemeChange(AppTheme.SYSTEM)
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        label = { Text(label) },
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Slate200
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = Slate500,
            iconColor = Slate500,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ── Bagian 2: Transaksi ─────────────────────────────────────────────────────

@Composable
private fun TransactionSection(
    settings: com.rising.pos.core.datastore.BusinessSettings,
    onUpdateFeatureToggles: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateTaxAndService: (Boolean, Double, Boolean, Boolean, Double) -> Unit
) {
    SettingsCard(title = "Fitur toko", icon = Icons.Outlined.SettingsSuggest) {
        Text(
            "Aktifkan atau matikan fitur sesuai kebutuhan usaha Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        SettingToggleRow(
            title = "Pencatatan stok",
            subtitle = "Otomatis catat stok masuk, keluar, dan sisa produk",
            icon = Icons.Outlined.Inventory2,
            checked = settings.isStockTrackingEnabled,
            onCheckedChange = {
                onUpdateFeatureToggles(
                    settings.isTableEnabled,
                    settings.isModifierEnabled,
                    settings.isBarcodeEnabled,
                    it
                )
            }
        )
        SettingToggleRow(
            title = "Nomor meja",
            subtitle = "Cocok untuk kedai, restoran, atau kafe dine-in",
            icon = Icons.Outlined.TableRestaurant,
            checked = settings.isTableEnabled,
            onCheckedChange = {
                onUpdateFeatureToggles(
                    it,
                    settings.isModifierEnabled,
                    settings.isBarcodeEnabled,
                    settings.isStockTrackingEnabled
                )
            }
        )
        SettingToggleRow(
            title = "Topping dan modifier",
            subtitle = "Opsi tambahan seperti extra shot, gula, atau topping",
            icon = Icons.Outlined.Bolt,
            checked = settings.isModifierEnabled,
            onCheckedChange = {
                onUpdateFeatureToggles(
                    settings.isTableEnabled,
                    it,
                    settings.isBarcodeEnabled,
                    settings.isStockTrackingEnabled
                )
            }
        )
        SettingToggleRow(
            title = "Barcode scanner",
            subtitle = "Scan barcode fisik atau kamera untuk mencari produk",
            icon = Icons.Outlined.QrCodeScanner,
            checked = settings.isBarcodeEnabled,
            onCheckedChange = {
                onUpdateFeatureToggles(
                    settings.isTableEnabled,
                    settings.isModifierEnabled,
                    it,
                    settings.isStockTrackingEnabled
                )
            }
        )
    }

    SettingsCard(title = "Pajak dan biaya layanan", icon = Icons.Outlined.CreditCard) {
        SettingToggleRow(
            title = "Aktifkan pajak (PPN / PB1)",
            subtitle = "${settings.taxPercentage}% · ${if (settings.isTaxInclusive) "sudah termasuk" else "ditambahkan"}",
            checked = settings.isTaxEnabled,
            onCheckedChange = {
                onUpdateTaxAndService(
                    it,
                    settings.taxPercentage,
                    settings.isTaxInclusive,
                    settings.isServiceChargeEnabled,
                    settings.serviceChargePercentage
                )
            }
        )
        SettingToggleRow(
            title = "Biaya layanan (service charge)",
            subtitle = "${settings.serviceChargePercentage}% dari subtotal",
            checked = settings.isServiceChargeEnabled,
            onCheckedChange = {
                onUpdateTaxAndService(
                    settings.isTaxEnabled,
                    settings.taxPercentage,
                    settings.isTaxInclusive,
                    it,
                    settings.serviceChargePercentage
                )
            }
        )
    }
}

// ── Bagian 3: Perangkat ─────────────────────────────────────────────────────

@Composable
private fun DeviceSection(
    settings: com.rising.pos.core.datastore.BusinessSettings,
    isTestingPrint: Boolean,
    printerStatusMessage: String?,
    onOpenPrinterPicker: () -> Unit,
    onSetPaperWidth: (Int) -> Unit,
    onSetAutoPrint: (Boolean) -> Unit,
    onTestPrint: () -> Unit
) {
    val hasPrinter = settings.printerMacAddress.isNotBlank()

    SettingsCard(title = "Printer struk", icon = Icons.Outlined.Print) {
        Text(
            "Hubungkan printer thermal Bluetooth untuk mencetak struk kasir.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (hasPrinter) settings.printerName else "Belum ada printer dipilih",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900
                    )
                    Text(
                        text = if (hasPrinter) settings.printerMacAddress else "Ketuk pilih printer untuk menghubungkan",
                        style = if (hasPrinter) {
                            MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace)
                        } else {
                            MaterialTheme.typography.labelMedium
                        },
                        color = if (hasPrinter) MaterialTheme.colorScheme.primary else Slate500
                    )
                }
                OutlinedButton(
                    onClick = onOpenPrinterPicker,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Text("Pilih printer", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        }

        Text("Ukuran kertas struk", style = MaterialTheme.typography.labelLarge, color = Slate700)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaperWidthChip("58 mm", settings.printerPaperWidthMm == 58) { onSetPaperWidth(58) }
            PaperWidthChip("80 mm", settings.printerPaperWidthMm == 80) { onSetPaperWidth(80) }
        }

        SettingToggleRow(
            title = "Cetak struk otomatis",
            subtitle = "Langsung cetak struk setelah transaksi berhasil",
            checked = settings.autoPrintReceipt,
            onCheckedChange = onSetAutoPrint
        )

        Button(
            onClick = onTestPrint,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isTestingPrint && hasPrinter,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isTestingPrint) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Mengirim ke printer...", style = MaterialTheme.typography.labelLarge, color = Color.White)
            } else {
                Text("Cetak percobaan", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }

        if (!hasPrinter) {
            Text(
                "Pilih printer terlebih dahulu untuk mencetak percobaan.",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
        }

        printerStatusMessage?.let { msg ->
            PrinterFeedback(msg)
        }
    }
}

@Composable
private fun PaperWidthChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Slate200
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = Slate500,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun PrinterFeedback(message: String) {
    val success = message.contains("berhasil", ignoreCase = true)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (success) SuccessGreenContainer else DangerRedContainer
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (success) SuccessGreen else DangerRed,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// ── Bagian 4: Data dan akses ────────────────────────────────────────────────

@Composable
private fun DataSection(
    settings: com.rising.pos.core.datastore.BusinessSettings,
    uiState: SettingsUiState,
    updateState: com.rising.pos.core.update.model.UpdateState,
    context: android.content.Context,
    viewModel: SettingsViewModel,
    onOpenPinSetup: () -> Unit,
    onRestoreClick: () -> Unit
) {
    val busy = uiState.isExporting || uiState.isRestoring

    SettingsCard(title = "Ekspor data", icon = Icons.Outlined.CloudUpload) {
        Text(
            "Unduh data usaha dalam format spreadsheet (.csv).",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        SettingActionRow(
            title = "Ekspor transaksi",
            subtitle = "Riwayat penjualan",
            icon = Icons.Outlined.ReceiptLong,
            enabled = !uiState.isExporting,
            onClick = { viewModel.exportTransactions(context) }
        )
        SettingActionRow(
            title = "Ekspor produk",
            subtitle = "Daftar barang dan harga",
            icon = Icons.Outlined.Inventory2,
            enabled = !uiState.isExporting,
            onClick = { viewModel.exportProducts(context) }
        )
        SettingActionRow(
            title = "Ekspor biaya",
            subtitle = "Catatan pengeluaran",
            icon = Icons.Outlined.CreditCard,
            enabled = !busy,
            onClick = { viewModel.exportExpenses(context) }
        )
        SettingActionRow(
            title = "Ekspor pelanggan",
            subtitle = "Kontak dan riwayat belanja",
            icon = Icons.Outlined.People,
            enabled = !busy,
            onClick = { viewModel.exportCustomers(context) }
        )

        uiState.exportStatusMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = if (msg.contains("berhasil", ignoreCase = true)) SuccessGreen else DangerRed
            )
        }
    }

    SettingsCard(title = "Cadangan dan pemulihan", icon = Icons.Outlined.CloudDownload) {
        Text(
            "Buat salinan database lokal, atau pulihkan dari file cadangan.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { viewModel.backupDatabase(context) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !busy
            ) {
                Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cadangkan", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate200),
                enabled = !busy
            ) {
                Icon(Icons.Outlined.Restore, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Pulihkan", style = MaterialTheme.typography.labelLarge, color = DangerRed)
            }
        }

        if (uiState.isRestoring) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sedang memulihkan database...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
            }
        }

        uiState.restoreStatusMessage?.let { msg ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isRestoreSuccess) SuccessGreenContainer else DangerRedContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
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
    }

    SettingsCard(title = "Keamanan PIN owner", icon = Icons.Outlined.Lock) {
        Text(
            "Minta PIN owner sebelum kasir membatalkan transaksi atau memulihkan database.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (settings.isPinSecurityEnabled) "PIN aktif" else "PIN nonaktif",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (settings.isPinSecurityEnabled) SuccessGreen else Slate900
                )
                Text(
                    text = if (settings.isPinSecurityEnabled) {
                        "Aksi void dan pemulihan memerlukan verifikasi PIN."
                    } else {
                        "Kasir dapat membatalkan transaksi tanpa PIN."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
            OutlinedButton(
                onClick = onOpenPinSetup,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Text(
                    if (settings.isPinSecurityEnabled) "Ubah PIN" else "Atur PIN",
                    style = MaterialTheme.typography.labelLarge,
                    color = Slate700
                )
            }
        }
    }

    AppUpdateCard(
        updateState = updateState,
        onCheckUpdate = viewModel::checkForUpdates
    )
}

// ── Komponen bersama ────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Slate700, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = Slate900)
            }
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Slate900, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Baris aksi (navigasi/proses) dengan ikon, judul, dan chevron. */
@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Slate900)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
