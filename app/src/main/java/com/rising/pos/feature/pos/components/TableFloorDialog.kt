package com.rising.pos.feature.pos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TableFloorDialog(
    tables: List<RestaurantTableEntity>,
    heldOrders: List<TransactionWithDetails>,
    selectedTableNumber: String? = null,
    isSelectionMode: Boolean = false,
    currencySymbol: String = "Rp",
    onDismiss: () -> Unit,
    onSelectTable: (String) -> Unit,
    onResumeHeldOrder: (TransactionWithDetails) -> Unit = {},
    onSaveTable: (RestaurantTableEntity) -> Unit = {},
    onDeleteTable: (RestaurantTableEntity) -> Unit = {},
    onToggleOccupied: (tableNumber: String, isOccupied: Boolean) -> Unit = { _, _ -> },
    onSeedDefaultTables: () -> Unit = {}
) {
    var filterStatus by rememberSaveable { mutableStateOf("ALL") } // ALL, AVAILABLE, OCCUPIED
    var tableToEdit by remember { mutableStateOf<RestaurantTableEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var tableToDelete by remember { mutableStateOf<RestaurantTableEntity?>(null) }

    val occupiedCount = tables.count { it.isOccupied }
    val availableCount = tables.size - occupiedCount

    val filteredTables = when (filterStatus) {
        "AVAILABLE" -> tables.filter { !it.isOccupied }
        "OCCUPIED" -> tables.filter { it.isOccupied }
        else -> tables
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.TableRestaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isSelectionMode) "Pilih Meja Dine-In" else "Denah & Kelola Meja",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "$availableCount Kosong · $occupiedCount Terisi (Total ${tables.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Action Bar: Filter & Add Table Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filterStatus == "ALL",
                            onClick = { filterStatus = "ALL" },
                            label = { Text("Semua (${tables.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        FilterChip(
                            selected = filterStatus == "AVAILABLE",
                            onClick = { filterStatus = "AVAILABLE" },
                            label = { Text("Kosong ($availableCount)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = SuccessGreen
                            )
                        )
                        FilterChip(
                            selected = filterStatus == "OCCUPIED",
                            onClick = { filterStatus = "OCCUPIED" },
                            label = { Text("Terisi ($occupiedCount)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEDD5),
                                selectedLabelColor = Color(0xFFEA580C)
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            tableToEdit = null
                            showFormDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Slate200)
                Spacer(Modifier.height(12.dp))

                // Table List or Empty State
                if (tables.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Slate200.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.EventSeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Slate400
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Belum ada daftar meja",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tambahkan nomor meja untuk pesanan makan di tempat (Dine-in).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onSeedDefaultTables,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Buat Contoh (Meja 1-6)", fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    tableToEdit = null
                                    showFormDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Tambah Manual", fontSize = 13.sp)
                            }
                        }
                    }
                } else if (filteredTables.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Tidak ada meja pada kategori ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(filteredTables, key = { it.id }) { table ->
                            val isSelected = selectedTableNumber == table.tableNumber
                            // Find matching held transaction for this table if occupied
                            val associatedHeldTrx = if (table.isOccupied) {
                                heldOrders.firstOrNull { held ->
                                    held.transaction.id == table.currentTransactionId ||
                                        held.transaction.note?.contains("Meja: ${table.tableNumber}", ignoreCase = true) == true ||
                                        held.transaction.note?.equals(table.tableNumber, ignoreCase = true) == true
                                }
                            } else null

                            TableCardItem(
                                table = table,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                heldOrder = associatedHeldTrx,
                                currencySymbol = currencySymbol,
                                onSelect = {
                                    onSelectTable(table.tableNumber)
                                    onDismiss()
                                },
                                onResumeOrder = { trx ->
                                    onResumeHeldOrder(trx)
                                    onDismiss()
                                },
                                onEdit = {
                                    tableToEdit = table
                                    showFormDialog = true
                                },
                                onDelete = {
                                    tableToDelete = table
                                },
                                onToggleStatus = {
                                    onToggleOccupied(table.tableNumber, !table.isOccupied)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Form Dialog: Add / Edit Table
    if (showFormDialog) {
        TableFormDialog(
            table = tableToEdit,
            onDismiss = { showFormDialog = false },
            onSave = { updatedTable ->
                onSaveTable(updatedTable)
                showFormDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    tableToDelete?.let { table ->
        AlertDialog(
            onDismissRequest = { tableToDelete = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Hapus ${table.tableNumber}?", fontWeight = FontWeight.Bold, color = Slate900)
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus meja ini dari daftar?",
                    color = Slate500,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTable(table)
                        tableToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hapus Meja", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { tableToDelete = null }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun TableCardItem(
    table: RestaurantTableEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    heldOrder: TransactionWithDetails?,
    currencySymbol: String,
    onSelect: () -> Unit,
    onResumeOrder: (TransactionWithDetails) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (table.isOccupied) {
        Color(0xFFFED7AA)
    } else {
        Slate200
    }

    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else if (table.isOccupied) {
        Color(0xFFFFF7ED)
    } else {
        Color.White
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                if (isSelectionMode) {
                    onSelect()
                } else if (table.isOccupied && heldOrder != null) {
                    onResumeOrder(heldOrder)
                } else {
                    onSelect()
                }
            }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top: Table number and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.tableNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Slate400, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Capacity & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${table.capacity} Kursi",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (table.isOccupied) Color(0xFFFFEDD5) else Color(0xFFDCFCE7),
                    modifier = Modifier.clickable { onToggleStatus() }
                ) {
                    Text(
                        text = if (table.isOccupied) "TERISI" else "KOSONG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (table.isOccupied) Color(0xFFC2410C) else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info or Held Order Note if Occupied
            if (table.isOccupied) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFFED7AA).copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))

                if (heldOrder != null) {
                    Text(
                        text = CurrencyFormatter.format(heldOrder.transaction.grandTotal, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A3412)
                    )
                    Text(
                        text = "${heldOrder.items.size} item pesanan",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = { onResumeOrder(heldOrder) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Buka Pesanan", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Pesanan aktif di meja",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onToggleStatus,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    ) {
                        Text("Kosongkan Meja", fontSize = 10.sp, color = Slate700)
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Terpilih", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onSelect,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        Text(if (isSelectionMode) "Pilih" else "Mulai Pesan", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TableFormDialog(
    table: RestaurantTableEntity?,
    onDismiss: () -> Unit,
    onSave: (RestaurantTableEntity) -> Unit
) {
    var tableNumber by rememberSaveable { mutableStateOf(table?.tableNumber.orEmpty()) }
    var capacityText by rememberSaveable { mutableStateOf((table?.capacity ?: 4).toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickCapacities = listOf(2, 4, 6, 8)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (table == null) "Tambah Meja Baru" else "Edit ${table.tableNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = {
                        tableNumber = it
                        errorMessage = null
                    },
                    label = { Text("Nomor / Nama Meja") },
                    placeholder = { Text("Contoh: Meja 01, VIP 1, Luar A") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = DangerRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Kapasitas Kursi", style = MaterialTheme.typography.labelMedium, color = Slate700)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickCapacities.forEach { cap ->
                        FilterChip(
                            selected = capacityText == cap.toString(),
                            onClick = { capacityText = cap.toString() },
                            label = { Text("$cap Kursi") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            capacityText = it
                        }
                    },
                    label = { Text("Jumlah Kursi (Custom)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanNumber = tableNumber.trim()
                    if (cleanNumber.isBlank()) {
                        errorMessage = "Nomor meja tidak boleh kosong"
                        return@Button
                    }
                    val cap = capacityText.toIntOrNull() ?: 4
                    val entity = table?.copy(
                        tableNumber = cleanNumber,
                        capacity = if (cap <= 0) 4 else cap
                    ) ?: RestaurantTableEntity(
                        id = UUID.randomUUID().toString(),
                        tableNumber = cleanNumber,
                        capacity = if (cap <= 0) 4 else cap,
                        isOccupied = false
                    )
                    onSave(entity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Slate700)
            }
        }
    )
}
