package com.rising.pos.feature.product

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.foundation.layout.PaddingValues
import com.rising.pos.ui.components.WorkspaceHeader
import com.rising.pos.ui.components.WorkspaceEmptyState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate800
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.WarningAmber

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

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openAddProductForm,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Tambah produk") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            WorkspaceHeader("Produk", "${products.size} produk ditampilkan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                OutlinedButton(onClick = onNavigateToInventory, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kelola stok")
                }
                OutlinedButton(onClick = viewModel::openCategoryDialog, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kategori")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Cari nama, SKU, atau barcode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                items(categories, key = { it.id }) { cat ->
                    val isSelected = uiState.selectedCategoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat.id) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        Icons.Default.Inventory2,
                        if (uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null) "Produk tidak ditemukan" else "Buat katalog pertama",
                        if (uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null) "Coba kata kunci lain atau pilih kategori Semua."
                        else "Ketuk Tambah produk untuk mengisi nama, harga, dan stok barang jualan."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
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

    // Add / Edit Product Bottom Sheet
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

    // Add Category Dialog
    if (uiState.isCategoryDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeCategoryDialog,
            title = { Text("Tambah Kategori Baru") },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = viewModel::updateNewCategoryName,
                    label = { Text("Nama Kategori") },
                    placeholder = { Text("Contoh: Minuman Dingin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveCategory,
                    enabled = uiState.newCategoryName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeCategoryDialog) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk") },
            text = { Text("Apakah Anda yakin ingin menghapus '${item.product.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(item.product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.category?.name ?: "Tanpa kategori", style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(p.name, style = MaterialTheme.typography.titleMedium, color = Slate900)
            Text(CurrencyFormatter.format(p.sellingPrice, currencySymbol), style = MaterialTheme.typography.titleMedium, color = Slate900)
            if (p.costPrice > 0) Text("Modal ${CurrencyFormatter.format(p.costPrice, currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = Slate500)
            HorizontalDivider(color = Slate200, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (!p.trackStock) "Stok tidak dilacak" else if (isOutOfStock) "Stok habis" else "Stok ${p.stock.toInt()} ${p.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when { isOutOfStock -> DangerRed; isLowStock -> WarningAmber; else -> Slate500 },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEdit) { Text("Edit") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Hapus ${p.name}", tint = Slate500, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormBottomSheet(
    form: ProductFormState,
    categories: List<com.rising.pos.core.database.entity.CategoryEntity>,
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (form.name.isEmpty()) "Tambah Produk Baru" else "Edit Produk",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            HorizontalDivider(color = Slate200)

            OutlinedTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it)) },
                label = { Text("Nama Produk *") },
                placeholder = { Text("Contoh: Es Kopi Susu Gula Aren") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedCatName = categories.find { it.id == form.categoryId }?.name ?: "Pilih Kategori"
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
                        text = { Text("Tanpa Kategori") },
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.sellingPrice,
                    onValueChange = { onFormChange(form.copy(sellingPrice = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("Harga Jual *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = form.costPrice,
                    onValueChange = { onFormChange(form.copy(costPrice = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("Harga Modal") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Track stock toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kelola Stok Otomatis", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Stok berkurang otomatis saat checkout", style = MaterialTheme.typography.bodySmall.copy(color = Slate500))
                }
                Switch(
                    checked = form.trackStock,
                    onCheckedChange = { onFormChange(form.copy(trackStock = it)) }
                )
            }

            if (form.trackStock) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = form.stock,
                        onValueChange = { onFormChange(form.copy(stock = it.filter { ch -> ch.isDigit() })) },
                        label = { Text("Jumlah Stok") },
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
                    label = { Text("Batas Minimum Stok Menipis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            HorizontalDivider(color = Slate200)

            // Varian Produk Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Produk Memiliki Varian",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Contoh: Ukuran (Reguler/Large), Rasa, atau Level Pedas",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                }
                Switch(
                    checked = form.hasVariants,
                    onCheckedChange = { isChecked ->
                        if (isChecked && form.variants.isEmpty()) {
                            onAddVariant()
                        } else {
                            onFormChange(form.copy(hasVariants = isChecked))
                        }
                    }
                )
            }

            if (form.hasVariants) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Daftar Varian",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    )

                    form.variants.forEachIndexed { index, variant ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Varian #${index + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                    )
                                    IconButton(
                                        onClick = { onRemoveVariant(variant.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Hapus Varian",
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
                                    label = { Text("Nama Varian *") },
                                    placeholder = { Text("Contoh: Regular / Extra") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                        label = { Text("Harga Jual") },
                                        prefix = { Text("$currencySymbol ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
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
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onAddVariant,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Tambah Varian")
                    }
                }
            }

            OutlinedTextField(
                value = form.barcode,
                onValueChange = { onFormChange(form.copy(barcode = it)) },
                label = { Text("Barcode (Opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSave,
                enabled = form.name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan Produk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
