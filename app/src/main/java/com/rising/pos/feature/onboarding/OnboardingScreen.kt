package com.rising.pos.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.model.BusinessType
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900

/**
 * Layar setup awal: mengisi identitas usaha sebelum mulai berjualan.
 *
 * Struktur: sambutan singkat, lalu tiga kelompok yang jelas (Jenis usaha,
 * Profil usaha, Perangkat), lalu tombol utama yang selalu terlihat di bawah.
 * Tombol tidak aktif sampai nama toko terisi, dan alasan mengapa tidak aktif
 * dijelaskan lewat teks bantu.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val nameValid = state.businessName.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            // Konten yang bisa di-scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(28.dp))

                // Sambutan
                Box(
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Selamat datang", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    text = "Isi data usaha Anda sekali saja. Setelah ini, Anda bisa langsung mulai berjualan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Kelompok 1: jenis usaha
                SectionLabel("Jenis usaha")
                Spacer(Modifier.height(10.dp))
                BusinessTypeGrid(
                    selected = state.businessType,
                    onSelect = viewModel::updateType
                )

                Spacer(Modifier.height(24.dp))

                // Kelompok 2: profil usaha
                SectionLabel("Profil usaha")
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = state.businessName,
                            onValueChange = viewModel::updateName,
                            label = { Text("Nama toko atau usaha") },
                            placeholder = { Text("Contoh: Kedai Kopi Rising") },
                            leadingIcon = {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Slate500)
                            },
                            isError = state.businessName.isNotEmpty() && !nameValid,
                            supportingText = if (state.businessName.isEmpty()) {
                                { Text("Wajib diisi") }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = viewModel::updatePhone,
                            label = { Text("Nomor telepon / WhatsApp") },
                            placeholder = { Text("Opsional") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = state.address,
                            onValueChange = viewModel::updateAddress,
                            label = { Text("Alamat usaha") },
                            placeholder = { Text("Opsional") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Kelompok 3: perangkat
                SectionLabel("Perangkat")
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = state.deviceId,
                            onValueChange = viewModel::updateDeviceId,
                            label = { Text("Kode perangkat") },
                            leadingIcon = {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = Slate500)
                            },
                            supportingText = {
                                Text("Dipakai pada nomor struk. Contoh: A01 untuk kasir utama, B01 untuk kasir kedua.")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // CTA sticky di dasar, selalu terlihat
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (!nameValid) {
                    Text(
                        text = "Isi nama toko untuk melanjutkan.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.submitOnboarding(onFinish) },
                    enabled = !state.isLoading && nameValid,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Mulai berjualan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = Slate900)
}

@Composable
private fun BusinessTypeGrid(
    selected: BusinessType,
    onSelect: (BusinessType) -> Unit
) {
    val options = listOf(
        Triple(BusinessType.WARUNG, "Warung", Icons.Outlined.Storefront),
        Triple(BusinessType.CAFE, "Kafe / Kedai", Icons.Outlined.Coffee),
        Triple(BusinessType.RETAIL, "Retail / Toko", Icons.Outlined.ShoppingBag),
        Triple(BusinessType.SERVICE, "Jasa", Icons.Outlined.HomeWork)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (type, label, icon) ->
                    BusinessTypeCard(
                        label = label,
                        icon = icon,
                        selected = selected == type,
                        onClick = { onSelect(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BusinessTypeCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Slate200
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else Slate500,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else Slate700
            )
        }
    }
}
