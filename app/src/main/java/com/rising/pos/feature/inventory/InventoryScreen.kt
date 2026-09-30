package com.rising.pos.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trackedProducts by viewModel.trackedProducts.collectAsState()
    val recentMovements by viewModel.recentMovements.collectAsState()

    val tabs = listOf("Daftar Stok Produk", "Riwayat Pergerakan Stok")
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inventori & Stok",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Pantau keluar masuk & penyesuaian barang",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }

            Button(
                onClick = { viewModel.openStockAction(StockActionType.RESTOCK) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stok Masuk", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = Color.Transparent,
            divider = { HorizontalDivider(color = Slate200) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == index) PrimaryBlue else Slate500
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.selectedTab == 0) {
            // Tab 1: Tracked Products
            if (trackedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Slate500, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada produk dengan pelacakan stok.", style = MaterialTheme.typography.bodyMedium.copy(color = Slate500))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trackedProducts, key = { it.id }) { product ->
                        ProductStockRow(
                            product = product,
                            onRestock = { viewModel.openStockAction(StockActionType.RESTOCK, product) },
                            onAdjust = { viewModel.openStockAction(StockActionType.ADJUSTMENT, product) },
                            onDamage = { viewModel.openStockAction(StockActionType.DAMAGE_LOSS, product) }
                        )
                    }
                }
            }
        } else {
            // Tab 2: Movement Log
            if (recentMovements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Slate500, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada pergerakan stok tercatat.", style = MaterialTheme.typography.bodyMedium.copy(color = Slate500))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentMovements, key = { it.id }) { movement ->
                        val matchedProduct = trackedProducts.find { it.id == movement.productId }
                        MovementItemRow(
                            movement = movement,
                            productName = matchedProduct?.name ?: "Produk #${movement.productId.take(6)}",
                            unit = matchedProduct?.unit ?: "item",
                            dateStr = dateFormatter.format(Date(movement.createdAt))
                        )
                    }
                }
            }
        }
    }

    // Stock Action Form BottomSheet
    if (uiState.isFormOpen) {
        StockActionBottomSheet(
            form = uiState.formState,
            products = trackedProducts,
            onSelectProduct = viewModel::updateSelectedProduct,
            onAmountChange = viewModel::updateAmount,
            onReasonChange = viewModel::updateReason,
            onDismiss = viewModel::closeForm,
            onSubmit = viewModel::submitStockAction
        )
    }
}

@Composable
private fun ProductStockRow(
    product: ProductEntity,
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onDamage: () -> Unit
) {
    val isOutOfStock = product.stock <= 0
    val isLowStock = product.stock > 0 && product.stock <= product.minStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val stockColor = when {
                        isOutOfStock -> DangerRed
                        isLowStock -> WarningAmber
                        else -> SuccessGreen
                    }
                    Text(
                        text = "Stok Saat Ini: ${product.stock.toInt()} ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = stockColor
                        )
                    )
                    Text(
                        text = " (Min: ${product.minStock.toInt()})",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                }
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onRestock,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("+ Masuk", fontSize = 11.sp, color = SuccessGreen)
                }
                IconButton(onClick = onAdjust) {
                    Icon(Icons.Default.Edit, contentDescription = "Koreksi", tint = Slate700, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDamage) {
                    Icon(Icons.Default.Warning, contentDescription = "Rusak", tint = DangerRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MovementItemRow(
    movement: StockMovementEntity,
    productName: String,
    unit: String,
    dateStr: String
) {
    val (badgeColor, typeLabel) = when (movement.type) {
        StockMovementType.IN -> SuccessGreen to "MASUK"
        StockMovementType.OUT -> DangerRed to "KELUAR"
        StockMovementType.SALE -> PrimaryBlue to "PENJUALAN"
        StockMovementType.ADJUSTMENT -> WarningAmber to "KOREKSI"
        StockMovementType.REFUND -> PrimaryBlue to "REFUND / RETUR"
        StockMovementType.DAMAGE -> DangerRed to "RUSAK"
        StockMovementType.LOSS -> DangerRed to "HILANG"
    }


    val deltaSign = if (movement.qtyChange > 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Slate900))
                }

                if (!movement.reason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = movement.reason, style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                }
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$deltaSign${movement.qtyChange.toInt()} $unit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (movement.qtyChange >= 0) SuccessGreen else DangerRed
                    )
                )
                Text(
                    text = "${movement.qtyBefore.toInt()} → ${movement.qtyAfter.toInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockActionBottomSheet(
    form: StockFormState,
    products: List<ProductEntity>,
    onSelectProduct: (ProductEntity) -> Unit,
    onAmountChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val title = when (form.actionType) {
        StockActionType.RESTOCK -> "Catat Stok Masuk (Kulakan)"
        StockActionType.ADJUSTMENT -> "Koreksi Fisik (Stock Opname)"
        StockActionType.DAMAGE_LOSS -> "Catat Stok Rusak / Hilang"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Slate900))

            HorizontalDivider(color = Slate200)

            // Product Dropdown
            ExposedDropdownMenuBox(
                expanded = productDropdownExpanded,
                onExpandedChange = { productDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = form.selectedProduct?.name ?: "Pilih Produk",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Produk *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = productDropdownExpanded,
                    onDismissRequest = { productDropdownExpanded = false }
                ) {
                    products.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.name} (Stok: ${p.stock.toInt()} ${p.unit})") },
                            onClick = {
                                onSelectProduct(p)
                                productDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            val amountLabel = when (form.actionType) {
                StockActionType.RESTOCK -> "Jumlah Stok Masuk"
                StockActionType.ADJUSTMENT -> "Jumlah Stok Sebenarnya Sekarang"
                StockActionType.DAMAGE_LOSS -> "Jumlah Stok Rusak / Hilang"
            }

            OutlinedTextField(
                value = form.amountInput,
                onValueChange = onAmountChange,
                label = { Text(amountLabel) },
                suffix = { form.selectedProduct?.let { Text(it.unit) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = form.reasonInput,
                onValueChange = onReasonChange,
                label = { Text("Keterangan / Alasan (Opsional)") },
                placeholder = { Text("Contoh: Kulakan supplier A / Basi / Salah hitung") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSubmit,
                enabled = form.selectedProduct != null && form.amountInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (form.actionType) {
                        StockActionType.RESTOCK -> SuccessGreen
                        StockActionType.ADJUSTMENT -> WarningAmber
                        StockActionType.DAMAGE_LOSS -> DangerRed
                    }
                )
            ) {
                Text("Simpan Perubahan Stok", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
