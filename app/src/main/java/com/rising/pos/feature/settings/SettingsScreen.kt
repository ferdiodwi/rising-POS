package com.rising.pos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.rising.pos.core.model.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import com.rising.pos.ui.components.PinSetupDialog
import com.rising.pos.ui.components.SecurityPinDialog
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.feature.settings.components.AppUpdateCard
import com.rising.pos.feature.settings.components.AppUpdateDialog
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.WarningAmber

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

    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Text(
                text = "Konfigurasi Toko & Fitur Modular",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )
        }

        // Section 0: Tema & Tampilan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tema & Tampilan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    text = "Pilih tema aplikasi yang diinginkan (Tema Gelap menggunakan warna hitam OLED).",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.appTheme == AppTheme.LIGHT,
                        onClick = { viewModel.setAppTheme(AppTheme.LIGHT) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Terang", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = settings.appTheme == AppTheme.DARK,
                        onClick = { viewModel.setAppTheme(AppTheme.DARK) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Gelap (Hitam)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = settings.appTheme == AppTheme.SYSTEM,
                        onClick = { viewModel.setAppTheme(AppTheme.SYSTEM) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Sistem", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 1: Profil Toko & Device
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Profil Usaha & Struk",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )

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
                    label = { Text("No. Telepon / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = footerNote,
                    onValueChange = { footerNote = it },
                    label = { Text("Catatan Footer Struk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("Device ID (Prefix Struk)") },
                    supportingText = { Text("Contoh: A01 untuk kasir 1, B01 untuk kasir 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { viewModel.updateBusinessProfile(name, phone, address, footerNote, deviceId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan Profil")
                }
            }
        }

        // Section 2: Fitur Modular (PRD Section 8)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Fitur Modular (Modular Config)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    text = "Aktifkan atau matikan fitur sesuai dengan jenis bisnis Anda.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                HorizontalDivider(color = Slate200)

                SettingToggleRow(
                    title = "Pencatatan Stok",
                    subtitle = "Otomatis catat stok masuk, keluar, dan sisa produk",
                    checked = settings.isStockTrackingEnabled,
                    onCheckedChange = {
                        viewModel.updateFeatureToggles(
                            isTableEnabled = settings.isTableEnabled,
                            isModifierEnabled = settings.isModifierEnabled,
                            isBarcodeEnabled = settings.isBarcodeEnabled,
                            isStockTrackingEnabled = it
                        )
                    }
                )

                SettingToggleRow(
                    title = "Nomor Meja",
                    subtitle = "Cocok untuk kedai, restoran, atau kafe dine-in",
                    checked = settings.isTableEnabled,
                    onCheckedChange = {
                        viewModel.updateFeatureToggles(
                            isTableEnabled = it,
                            isModifierEnabled = settings.isModifierEnabled,
                            isBarcodeEnabled = settings.isBarcodeEnabled,
                            isStockTrackingEnabled = settings.isStockTrackingEnabled
                        )
                    }
                )

                SettingToggleRow(
                    title = "Topping & Modifier",
                    subtitle = "Pilihan opsi tambahan seperti extra shot, gula, topping",
                    checked = settings.isModifierEnabled,
                    onCheckedChange = {
                        viewModel.updateFeatureToggles(
                            isTableEnabled = settings.isTableEnabled,
                            isModifierEnabled = it,
                            isBarcodeEnabled = settings.isBarcodeEnabled,
                            isStockTrackingEnabled = settings.isStockTrackingEnabled
                        )
                    }
                )

                SettingToggleRow(
                    title = "Barcode Scanner",
                    subtitle = "Scan barcode fisik atau kamera untuk mencari produk",
                    checked = settings.isBarcodeEnabled,
                    onCheckedChange = {
                        viewModel.updateFeatureToggles(
                            isTableEnabled = settings.isTableEnabled,
                            isModifierEnabled = settings.isModifierEnabled,
                            isBarcodeEnabled = it,
                            isStockTrackingEnabled = settings.isStockTrackingEnabled
                        )
                    }
                )
            }
        }

        // Section 3: Pajak & Biaya Layanan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pajak & Biaya Layanan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )

                SettingToggleRow(
                    title = "Aktifkan Pajak (PPN / PB1)",
                    subtitle = "${settings.taxPercentage}% (Termasuk: ${if (settings.isTaxInclusive) "Ya" else "Tidak"})",
                    checked = settings.isTaxEnabled,
                    onCheckedChange = {
                        viewModel.updateTaxAndService(
                            isTaxEnabled = it,
                            taxPercentage = settings.taxPercentage,
                            isTaxInclusive = settings.isTaxInclusive,
                            isServiceChargeEnabled = settings.isServiceChargeEnabled,
                            serviceChargePercentage = settings.serviceChargePercentage
                        )
                    }
                )

                SettingToggleRow(
                    title = "Biaya Layanan (Service Charge)",
                    subtitle = "${settings.serviceChargePercentage}%",
                    checked = settings.isServiceChargeEnabled,
                    onCheckedChange = {
                        viewModel.updateTaxAndService(
                            isTaxEnabled = settings.isTaxEnabled,
                            taxPercentage = settings.taxPercentage,
                            isTaxInclusive = settings.isTaxInclusive,
                            isServiceChargeEnabled = it,
                            serviceChargePercentage = settings.serviceChargePercentage
                        )
                    }
                )
            }
        }

        // Section 4: Printer Struk Thermal Bluetooth (PRD Section 7.33)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Printer Struk Thermal (ESC/POS)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    text = "Hubungkan printer thermal Bluetooth untuk mencetak struk kasir secara instan.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                HorizontalDivider(color = Slate200)

                // Current Connected Printer Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (settings.printerName.isNotBlank()) settings.printerName else "Belum Ada Printer Dipilih",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                        )
                        Text(
                            text = if (settings.printerMacAddress.isNotBlank()) settings.printerMacAddress else "Ketuk 'Pilih Printer' untuk menghubungkan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (settings.printerMacAddress.isNotBlank()) PrimaryBlue else Slate500,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::openPrinterPicker,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pilih Printer", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = Slate200)

                // Paper Width Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ukuran Kertas Struk:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = Slate700)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = settings.printerPaperWidthMm == 58,
                            onClick = { viewModel.setPaperWidth(58) },
                            label = { Text("58 mm (Standar Mini)") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = settings.printerPaperWidthMm == 80,
                            onClick = { viewModel.setPaperWidth(80) },
                            label = { Text("80 mm (Lebar)") }
                        )
                    }
                }

                SettingToggleRow(
                    title = "Cetak Struk Otomatis",
                    subtitle = "Langsung cetak struk sesaat setelah transaksi kasir berhasil",
                    checked = settings.autoPrintReceipt,
                    onCheckedChange = viewModel::setAutoPrint
                )

                // Test Print Button & Feedback
                Button(
                    onClick = viewModel::testPrint,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !uiState.isTestingPrint && settings.printerMacAddress.isNotBlank(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.rising.pos.ui.theme.PrimaryBlue
                    )
                ) {
                    if (uiState.isTestingPrint) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengirim ke Printer...")
                    } else {
                        Text("Uji Cetak (Test Print)")
                    }
                }

                uiState.printerStatusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (msg.contains("berhasil", ignoreCase = true)) com.rising.pos.ui.theme.SuccessGreen else com.rising.pos.ui.theme.DangerRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Section 5: Ekspor Data & Backup Database (PRD 7.35 & 7.36)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ekspor Data & Cadangan (Backup)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    text = "Unduh dan bagikan data usaha ke format spreadsheet (.csv) atau buat salinan database lokal.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                HorizontalDivider(color = Slate200)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.exportTransactions(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isExporting
                    ) {
                        Text("Ekspor Transaksi", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportProducts(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isExporting
                    ) {
                        Text("Ekspor Produk", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.exportExpenses(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isExporting && !uiState.isRestoring
                    ) {
                        Text("Ekspor Biaya", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportCustomers(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isExporting && !uiState.isRestoring
                    ) {
                        Text("Ekspor Pelanggan", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.backupDatabase(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !uiState.isExporting && !uiState.isRestoring
                    ) {
                        Text("Cadangkan DB", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (settings.isPinSecurityEnabled && settings.securityPin.isNotBlank()) {
                                isPinAuthForRestoreOpen = true
                            } else {
                                isRestoreConfirmationOpen = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isExporting && !uiState.isRestoring
                    ) {
                        Text("Pulihkan DB", fontSize = 12.sp, color = DangerRed)
                    }
                }

                if (uiState.isRestoring) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sedang memulihkan database...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }
                }

                uiState.restoreStatusMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isRestoreSuccess) SuccessGreen.copy(alpha = 0.12f) else DangerRed.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.isRestoreSuccess) SuccessGreen else DangerRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            TextButton(onClick = viewModel::clearRestoreStatus) {
                                Text("Tutup", fontSize = 11.sp)
                            }
                        }
                    }
                }

                uiState.exportStatusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (msg.contains("berhasil", ignoreCase = true)) SuccessGreen else DangerRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Section 6: Keamanan & PIN Owner (PRD 7.37 & MVP #18)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Keamanan & PIN Owner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    text = "Kunci otorisasi untuk aksi penting seperti pembatalan (Void) transaksi kasir.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                HorizontalDivider(color = Slate200)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (settings.isPinSecurityEnabled) "PIN Aktif" else "PIN Nonaktif",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (settings.isPinSecurityEnabled) SuccessGreen else Slate500
                            )
                        )
                        Text(
                            text = if (settings.isPinSecurityEnabled) "Aksi Void memerlukan verifikasi PIN" else "Kasir dapat membatalkan transaksi tanpa PIN",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::openPinSetupDialog,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (settings.isPinSecurityEnabled) "Ubah PIN" else "Atur PIN", fontSize = 12.sp)
                    }
                }
            }
        }

        // ── In-App Update (GitHub Releases) ───────────────────────────────────
        AppUpdateCard(
            updateState = updateState,
            onCheckUpdate = viewModel::checkForUpdates
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (uiState.isPrinterPickerOpen) {
        com.rising.pos.feature.settings.components.PrinterPickerDialog(
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
        onDownload = { url, tag -> viewModel.downloadUpdate(url, tag) },
        onInstall = viewModel::installUpdate,
        onOpenUnknownSourcesSettings = {
            viewModel.getUnknownSourcesSettingsIntent()?.let { intent ->
                context.startActivity(intent)
            }
        },
        onDismiss = viewModel::dismissUpdate
    )

    if (isPinAuthForRestoreOpen) {
        SecurityPinDialog(
            correctPin = settings.securityPin,
            title = "Otorisasi Pulihkan Database",
            description = "Masukkan PIN Owner untuk melanjutkan proses pemulihan database.",
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
            title = {
                Text(
                    text = "Konfirmasi Pemulihan Database",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DangerRed)
                )
            },
            text = {
                Text(
                    text = "PERINGATAN: Memulihkan database akan mengganti seluruh data yang ada saat ini (transaksi, produk, pelanggan, biaya) dengan data dari file cadangan yang dipilih.\n\nApakah Anda yakin ingin melanjutkan dan memilih file cadangan (.db)?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoreConfirmationOpen = false
                        restoreFilePickerLauncher.launch("*/*")
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Pilih File Cadangan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isRestoreConfirmationOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }
}


@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Slate900))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
