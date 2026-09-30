package com.rising.pos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    var name by remember(settings.name) { mutableStateOf(settings.name) }
    var phone by remember(settings.phone) { mutableStateOf(settings.phone) }
    var address by remember(settings.address) { mutableStateOf(settings.address) }
    var footerNote by remember(settings.footerNote) { mutableStateOf(settings.footerNote) }
    var deviceId by remember(settings.deviceId) { mutableStateOf(settings.deviceId) }

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

        // Section 1: Profil Toko & Device
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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

        Spacer(modifier = Modifier.height(20.dp))
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
