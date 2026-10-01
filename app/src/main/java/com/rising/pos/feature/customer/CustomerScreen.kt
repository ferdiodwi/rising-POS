package com.rising.pos.feature.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.CustomerWithStats
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.customer.components.CustomerDetailDialog
import com.rising.pos.feature.customer.components.CustomerFormDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900

/**
 * Daftar pelanggan.
 *
 * Tiap baris: avatar inisial, nama, nomor telepon, dan total belanja (angka
 * menonjol, tabular). Aksi ada di halaman detail agar daftar tetap bersih.
 */
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val customerTransactions by viewModel.selectedCustomerTransactions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Button(
                onClick = viewModel::openAddCustomer,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Tambah pelanggan", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Pelanggan", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    "${customers.size} pelanggan ditampilkan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Cari nama atau nomor telepon") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = Icons.Outlined.People,
                        title = if (uiState.searchQuery.isBlank()) "Belum ada pelanggan" else "Pelanggan tidak ditemukan",
                        description = if (uiState.searchQuery.isBlank()) {
                            "Tambahkan pelanggan agar riwayat belanja mereka tercatat dan mudah dicari."
                        } else {
                            "Coba kata kunci lain."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.customer.id }) { item ->
                        CustomerCard(
                            customerWithStats = item,
                            currencySymbol = settings.currencySymbol,
                            onClick = { viewModel.selectCustomer(item) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.formState.isOpen) {
        CustomerFormDialog(
            formState = uiState.formState,
            onNameChange = viewModel::onFormNameChange,
            onPhoneChange = viewModel::onFormPhoneChange,
            onEmailChange = viewModel::onFormEmailChange,
            onAddressChange = viewModel::onFormAddressChange,
            onNotesChange = viewModel::onFormNotesChange,
            onDismiss = viewModel::closeForm,
            onSave = viewModel::saveCustomer
        )
    }

    uiState.selectedCustomer?.let { selected ->
        CustomerDetailDialog(
            customerWithStats = selected,
            transactions = customerTransactions,
            currencySymbol = settings.currencySymbol,
            onDismiss = { viewModel.selectCustomer(null) },
            onEdit = {
                viewModel.selectCustomer(null)
                viewModel.openEditCustomer(selected.customer)
            },
            onDelete = {
                viewModel.confirmDeleteCustomer(selected.customer)
            }
        )
    }

    uiState.customerToDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus pelanggan", style = MaterialTheme.typography.titleMedium, color = Slate900) },
            text = {
                Text(
                    "Pelanggan \"${toDelete.name}\" akan dihapus. Riwayat transaksi sebelumnya tetap tersimpan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::deleteConfirmedCustomer,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customerWithStats: CustomerWithStats,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val cust = customerWithStats.customer

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cust.name.trim().take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = cust.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!cust.phone.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = cust.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = CurrencyFormatter.format(customerWithStats.totalSpent, currencySymbol),
                    style = PosTextStyles.money,
                    color = Slate900
                )
                Text(
                    text = "${customerWithStats.totalTransactions} transaksi",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }

            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
