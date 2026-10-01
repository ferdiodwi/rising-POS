package com.rising.pos.feature.product

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen
import com.rising.pos.ui.theme.WarningAmber

/**
 * Manajemen produk.
 *
 * Daftar memakai baris (bukan grid) karena halaman ini untuk membaca dan
 * mengelola data: nama dan harga paling menonjol, aksi per produk ada di menu
 * agar baris tidak ramai. Form tambah/edit memakai bottom sheet dengan seksi
 * yang jelas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel = hiltViewModel(),
    onNavigateToInventory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()

    var productToDelete by remember { mutableStateOf<ProductWithCategory?>(null) }
    val isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Button(
                onClick = viewModel::openAddProductForm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Tambah produk", style = MaterialTheme.typography.titleMedium, color = Color.White)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Produk", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                    Text(
                        "${products.size} produk ditampilkan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                IconButton(onClick = onNavigateToInventory) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = "Kelola stok", tint = Slate700)
                }
                TextButton(onClick = viewModel::openCategoryDialog) {
                    Text("Kategori", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Cari nama, SKU, atau barcode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = Slate500)
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    CategoryFilterChip(
                        label = "Semua",
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    CategoryFilterChip(
                        label = cat.name,
                        selected = uiState.selectedCategoryId == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = if (isFiltered) Icons.Default.Search else Icons.Outlined.Inventory2,
                        title = if (isFiltered) "Produk tidak ditemukan" else "Belum ada produk",
                        description = if (isFiltered) {
                            "Coba kata kunci lain atau pilih kategori Semua."
                        } else {
                            "Ketuk Tambah produk untuk mengisi nama, harga, dan stok barang jualan."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.product.id }) { item ->
                        ProductItemRow(
                            item = item,
                            currencySymbol = settings.currencySymbol,
                            onEdit = { viewModel.openEditProductForm(item) },
                            onDelete = { productToDelete = item }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isFormOpen) {
        ProductFormBottomSheet(
            form = uiState.formState,
            categories = categories,
            currencySymbol = settings.currencySymbol,
            onFormChange = viewModel::updateForm,
            onAddVariant = viewModel::addVariant,
            onRemoveVariant = viewModel::removeVariant,
            onUpdateVariant = viewModel::updateVariant,
            onDismiss = viewModel::closeForm,
            onSave = viewModel::saveProduct
        )
    }

    if (uiState.isCategoryDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeCategoryDialog,
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Kategori baru", style = MaterialTheme.typography.titleMedium, color = Slate900) },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = viewModel::updateNewCategoryName,
                    label = { Text("Nama kategori") },
                    placeholder = { Text("Contoh: Minuman Dingin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveCategory,
                    enabled = uiState.newCategoryName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeCategoryDialog) {
                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        )
    }

    productToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus produk", style = MaterialTheme.typography.titleMedium, color = Slate900) },
            text = {
                Text(
                    "Produk \"${item.product.name}\" akan dihapus. Tindakan ini tidak bisa dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(item.product)
                        productToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal", style = MaterialTheme.typography.labelLarge, color = Slate700)
                }
            }
        )
    }
}

@Composable
private fun CategoryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.height(40.dp),
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
private fun ProductItemRow(
    item: ProductWithCategory,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = item.product
    val isOutOfStock = p.trackStock && p.stock <= 0
    val isLowStock = p.trackStock && p.stock > 0 && p.stock <= p.minStock
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = p.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.category?.name ?: "Tanpa kategori",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.format(p.sellingPrice, currencySymbol),
                    style = PosTextStyles.priceCard,
                    color = Slate900
                )
                if (p.costPrice > 0) {
                    Text(
                        text = "Modal ${CurrencyFormatter.format(p.costPrice, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                Spacer(Modifier.height(2.dp))
                StockLabel(
                    trackStock = p.trackStock,
                    isOutOfStock = isOutOfStock,
                    isLowStock = isLowStock,
                    stockLabel = "Stok ${p.stock.toInt()} ${p.unit}"
                )
            }

            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Aksi untuk ${p.name}",
                        tint = Slate500
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = Slate700, modifier = Modifier.size(20.dp))
                        },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = DangerRed) },
                        leadingIcon = {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                        },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StockLabel(
    trackStock: Boolean,
    isOutOfStock: Boolean,
    isLowStock: Boolean,
    stockLabel: String
) {
    val (icon, label, color) = when {
        !trackStock -> Triple(null, "Stok tidak dilacak", Slate500)
        isOutOfStock -> Triple(Icons.Outlined.Block, "Stok habis", DangerRed)
        isLowStock -> Triple(Icons.Outlined.WarningAmber, "$stockLabel · menipis", WarningAmber)
        else -> Triple(null, stockLabel, SuccessGreen)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormBottomSheet(
    form: ProductFormState,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    onFormChange: (ProductFormState) -> Unit,
    onAddVariant: () -> Unit,
    onRemoveVariant: (String) -> Unit,
    onUpdateVariant: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

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
            Text(
                text = if (form.name.isEmpty()) "Tambah produk" else "Edit produk",
                style = MaterialTheme.typography.titleLarge,
                color = Slate900
            )

            FormSection("Info dasar")
            OutlinedTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it)) },
                label = { Text("Nama produk") },
                placeholder = { Text("Contoh: Es Kopi Susu Gula Aren") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedCatName = categories.find { it.id == form.categoryId }?.name ?: "Tanpa kategori"
                OutlinedTextField(
                    value = selectedCatName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tanpa kategori") },
                        onClick = {
                            onFormChange(form.copy(categoryId = null))
                            categoryDropdownExpanded = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                onFormChange(form.copy(categoryId = cat.id))
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = form.barcode,
                onValueChange = { onFormChange(form.copy(barcode = it)) },
                label = { Text("Barcode (opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            FormSection("Harga")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.sellingPrice,
                    onValueChange = { onFormChange(form.copy(sellingPrice = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("Harga jual") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = form.costPrice,
                    onValueChange = { onFormChange(form.copy(costPrice = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("Harga modal") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            FormSection("Stok")
            ToggleRow(
                title = "Kelola stok otomatis",
                description = "Stok berkurang otomatis saat checkout",
                checked = form.trackStock,
                onCheckedChange = { onFormChange(form.copy(trackStock = it)) }
            )

            if (form.trackStock) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = form.stock,
                        onValueChange = { onFormChange(form.copy(stock = it.filter { ch -> ch.isDigit() })) },
                        label = { Text("Jumlah stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = form.unit,
                        onValueChange = { onFormChange(form.copy(unit = it)) },
                        label = { Text("Satuan") },
                        placeholder = { Text("pcs / porsi / botol") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = form.minStock,
                    onValueChange = { onFormChange(form.copy(minStock = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("Batas minimum stok menipis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            FormSection("Varian")
            ToggleRow(
                title = "Produk memiliki varian",
                description = "Contoh: ukuran (reguler/large), rasa, atau level pedas",
                checked = form.hasVariants,
                onCheckedChange = { isChecked ->
                    if (isChecked && form.variants.isEmpty()) {
                        onAddVariant()
                    } else {
                        onFormChange(form.copy(hasVariants = isChecked))
                    }
                }
            )

            if (form.hasVariants) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    form.variants.forEachIndexed { index, variant ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Varian ${index + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { onRemoveVariant(variant.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = "Hapus varian",
                                            tint = DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = variant.name,
                                    onValueChange = { newName ->
                                        onUpdateVariant(variant.id, newName, variant.price, variant.stock)
                                    },
                                    label = { Text("Nama varian") },
                                    placeholder = { Text("Contoh: Regular / Extra") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = variant.price,
                                        onValueChange = { newPrice ->
                                            onUpdateVariant(
                                                variant.id,
                                                variant.name,
                                                newPrice.filter { ch -> ch.isDigit() },
                                                variant.stock
                                            )
                                        },
                                        label = { Text("Harga jual") },
                                        prefix = { Text("$currencySymbol ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    if (form.trackStock) {
                                        OutlinedTextField(
                                            value = variant.stock,
                                            onValueChange = { newStock ->
                                                onUpdateVariant(
                                                    variant.id,
                                                    variant.name,
                                                    variant.price,
                                                    newStock.filter { ch -> ch.isDigit() }
                                                )
                                            },
                                            label = { Text("Stok") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onAddVariant,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tambah varian", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onSave,
                enabled = form.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan produk", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FormSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = Slate900)
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Slate900, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Slate500)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
