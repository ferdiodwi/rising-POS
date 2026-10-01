package com.rising.pos.feature.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.SuccessGreenContainer
import com.rising.pos.ui.theme.WarningAmber
import com.rising.pos.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Persediaan dan mutasi stok.
 *
 * Dua bagian dipisah dengan segmented control agar tidak menumpuk: Daftar stok
 * (dengan status berikon) dan Riwayat mutasi (dengan arah masuk/keluar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trackedProducts by viewModel.trackedProducts.collectAsState()
    val recentMovements by viewModel.recentMovements.collectAsState()

    val tabs = listOf("Persediaan", "Riwayat stok")
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Stok produk", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    "Pantau persediaan dan catat perubahan stok.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
            Button(
                onClick = { viewModel.openStockAction(StockActionType.RESTOCK) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Stok masuk", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }

        Spacer(Modifier.height(14.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                SegmentedButton(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = Slate500
                    )
                ) {
                    Text(title)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (uiState.selectedTab == 0) {
            if (trackedProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "Belum ada produk berstok",
                        description = "Aktifkan \"Kelola stok otomatis\" pada produk di menu Produk agar stoknya bisa dipantau di sini."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
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
            if (recentMovements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "Belum ada pergerakan stok",
                        description = "Catat stok masuk atau koreksi stok, maka riwayatnya akan muncul di sini."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = product.stock.toInt().toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = when {
                                isOutOfStock -> DangerRed
                                isLowStock -> WarningAmber
                                else -> Slate900
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = product.unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                StockStatusChip(isOutOfStock = isOutOfStock, isLowStock = isLowStock)
            }

            Text(
                text = "Batas minimum ${product.minStock.toInt()} ${product.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = Slate500
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRestock,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.NorthEast, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Masuk", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                }
                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, tint = Slate700, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Koreksi", style = MaterialTheme.typography.labelMedium, color = Slate700)
                }
                OutlinedButton(
                    onClick = onDamage,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.SouthWest, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Rusak", style = MaterialTheme.typography.labelMedium, color = DangerRed)
                }
            }
        }
    }
}

/** Status stok: ikon + teks, bukan warna saja. */
@Composable
private fun StockStatusChip(isOutOfStock: Boolean, isLowStock: Boolean) {
    val (container, content, icon, label) = when {
        isOutOfStock -> Quad(DangerRedContainer, DangerRed, Icons.Outlined.Block, "Habis")
        isLowStock -> Quad(WarningAmberContainer, WarningAmber, Icons.Outlined.WarningAmber, "Menipis")
        else -> Quad(SuccessGreenContainer, SuccessGreen, null, "Aman")
    }

    Surface(shape = RoundedCornerShape(999.dp), color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}

private data class Quad(
    val container: Color,
    val content: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val label: String
)

@Composable
private fun MovementItemRow(
    movement: StockMovementEntity,
    productName: String,
    unit: String,
    dateStr: String
) {
    val isIncoming = movement.qtyChange >= 0
    val typeLabel = when (movement.type) {
        StockMovementType.IN -> "Masuk"
        StockMovementType.OUT -> "Keluar"
        StockMovementType.SALE -> "Penjualan"
        StockMovementType.ADJUSTMENT -> "Koreksi"
        StockMovementType.REFUND -> "Refund"
        StockMovementType.DAMAGE -> "Rusak"
        StockMovementType.LOSS -> "Hilang"
    }
    val accent = if (isIncoming) SuccessGreen else DangerRed
    val deltaSign = if (movement.qtyChange > 0) "+" else ""

    Card(
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
            // Ikon arah: masuk atau keluar
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isIncoming) SuccessGreenContainer else DangerRedContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isIncoming) Icons.Outlined.NorthEast else Icons.Outlined.SouthWest,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(typeLabel, style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
                if (!movement.reason.isNullOrBlank()) {
                    Text(
                        text = movement.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$dateStr · ${movement.qtyBefore.toInt()} jadi ${movement.qtyAfter.toInt()} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "$deltaSign${movement.qtyChange.toInt()}",
                style = PosTextStyles.priceCard,
                color = accent
            )
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
        StockActionType.RESTOCK -> "Catat stok masuk"
        StockActionType.ADJUSTMENT -> "Koreksi stok fisik"
        StockActionType.DAMAGE_LOSS -> "Catat stok rusak atau hilang"
    }

    val amountLabel = when (form.actionType) {
        StockActionType.RESTOCK -> "Jumlah stok masuk"
        StockActionType.ADJUSTMENT -> "Jumlah stok sebenarnya sekarang"
        StockActionType.DAMAGE_LOSS -> "Jumlah stok rusak atau hilang"
    }

    val accent = when (form.actionType) {
        StockActionType.RESTOCK -> SuccessGreen
        StockActionType.ADJUSTMENT -> WarningAmber
        StockActionType.DAMAGE_LOSS -> DangerRed
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Slate900)

            ExposedDropdownMenuBox(
                expanded = productDropdownExpanded,
                onExpandedChange = { productDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = form.selectedProduct?.name ?: "Pilih produk",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Produk") },
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
                            text = { Text("${p.name} (stok ${p.stock.toInt()} ${p.unit})") },
                            onClick = {
                                onSelectProduct(p)
                                productDropdownExpanded = false
                            }
                        )
                    }
                }
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
                label = { Text("Keterangan (opsional)") },
                placeholder = { Text("Contoh: Kulakan supplier A / Basi / Salah hitung") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Pratinjau stok setelah perubahan, agar kasir yakin sebelum menyimpan.
            StockPreview(form = form)

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onSubmit,
                enabled = form.selectedProduct != null && form.amountInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("Simpan perubahan stok", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StockPreview(form: StockFormState) {
    val product = form.selectedProduct ?: return
    val amount = form.amountInput.toDoubleOrNull() ?: return
    val unit = product.unit

    val after = when (form.actionType) {
        StockActionType.RESTOCK -> product.stock + amount
        StockActionType.ADJUSTMENT -> amount
        StockActionType.DAMAGE_LOSS -> product.stock - amount
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stok setelah perubahan", style = MaterialTheme.typography.bodyMedium, color = Slate700)
            Text(
                text = "${product.stock.toInt()} jadi ${after.toInt()} $unit",
                style = MaterialTheme.typography.titleSmall,
                color = Slate900
            )
        }
    }
}
