package com.rising.pos.feature.product

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import com.rising.pos.core.database.entity.ModifierEntity
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.ui.components.BarcodeScannerInputDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import java.io.File
import java.util.Locale
import java.util.UUID

import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate100
import com.rising.pos.ui.theme.Slate50
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.DangerRedContainer
import com.rising.pos.ui.theme.WarningAmber
import com.rising.pos.ui.theme.WarningAmberContainer
import com.rising.pos.ui.theme.PrimaryBlue

// Warna brand/status yang dipakai layar ini. Slate* kini berasal dari token
// adaptif (LocalPosColors) sehingga otomatis mengikuti mode terang/gelap.
private val BrandBlue = PrimaryBlue

private enum class StockFilterType {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK
}

/**
 * Layar Katalog Produk & Tambah/Ubah Produk sesuai style mockup:
 * - Header tebal "Produk" + Subtitle Toko + Scanner Icon
 * - Search bar rounded dengan outline halus
 * - Underline Tab bar kategori ("Semua", dll.)
 * - Subheader counter produk + Tombol Filter
 * - List produk dengan gambar/ikon kategori, harga tebal, stok, dan badge status
 * - Sticky bottom "+ Tambah produk"
 * - Fullscreen Form Tambah/Ubah Produk dengan input prefix [ Rp ] dan accordion pengaturan tambahan
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
    val modifiers by viewModel.modifiers.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()

    var productToDelete by remember { mutableStateOf<ProductWithCategory?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var stockFilter by remember { mutableStateOf(StockFilterType.ALL) }

    val displayedProducts = remember(products, stockFilter) {
        when (stockFilter) {
            StockFilterType.ALL -> products
            StockFilterType.LOW_STOCK -> products.filter {
                it.product.trackStock && it.product.stock > 0 && it.product.stock <= it.product.minStock
            }
            StockFilterType.OUT_OF_STOCK -> products.filter {
                it.product.trackStock && it.product.stock <= 0
            }
        }
    }

    val selectedTabIndex = remember(uiState.selectedCategoryId, categories) {
        if (uiState.selectedCategoryId == null) 0
        else {
            val idx = categories.indexOfFirst { it.id == uiState.selectedCategoryId }
            if (idx >= 0) idx + 1 else 0
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Slate100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = viewModel::openAddProductForm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Tambah produk",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Top Header Row ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.rising.pos.ui.components.PosHeaderTitleSection(
                    title = "Produk",
                    subtitle = settings.name.ifBlank { "Rising Studio" },
                    modifier = Modifier.weight(1f)
                )

                com.rising.pos.ui.components.PosHeaderActionButton(
                    icon = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan & Kelola Stok",
                    onClick = onNavigateToInventory
                )
            }

            // ── Search Bar ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                com.rising.pos.ui.components.PosSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    placeholder = "Cari nama atau kode barang"
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Category Underline Tabs ───────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = BrandBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(2.5.dp)
                                .background(BrandBlue)
                        )
                    }
                },
                divider = { HorizontalDivider(color = Slate100) }
            ) {
                // Tab "Semua"
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.selectCategory(null) },
                    text = {
                        Text(
                            text = "Semua",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == 0) BrandBlue else Slate500
                            )
                        )
                    }
                )

                // Tabs Kategori
                categories.forEachIndexed { index, cat ->
                    val isSelected = selectedTabIndex == index + 1
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat.id) },
                        text = {
                            Text(
                                text = cat.name,
                                maxLines = 1,
                                softWrap = false,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandBlue else Slate500
                                )
                            )
                        }
                    )
                }
            }

            // ── Subheader Row: Produk Count & Filter ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${displayedProducts.size} produk",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                )

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { filterMenuExpanded = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filter",
                            tint = if (stockFilter != StockFilterType.ALL) BrandBlue else Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when (stockFilter) {
                                StockFilterType.ALL -> "Filter"
                                StockFilterType.LOW_STOCK -> "Menipis"
                                StockFilterType.OUT_OF_STOCK -> "Habis"
                            },
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = if (stockFilter != StockFilterType.ALL) BrandBlue else Slate600,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua stok") },
                            onClick = {
                                stockFilter = StockFilterType.ALL
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Stok menipis") },
                            onClick = {
                                stockFilter = StockFilterType.LOW_STOCK
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Stok habis") },
                            onClick = {
                                stockFilter = StockFilterType.OUT_OF_STOCK
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Product Item List ─────────────────────────────────────────────
            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceEmptyState(
                        icon = if (uiState.searchQuery.isNotBlank()) Icons.Default.Search else Icons.Outlined.Inventory2,
                        title = if (uiState.searchQuery.isNotBlank()) "Produk tidak ditemukan" else "Belum ada produk",
                        description = if (uiState.searchQuery.isNotBlank()) {
                            "Coba kata kunci lain atau pilih kategori Semua."
                        } else {
                            "Ketuk Tambah produk untuk mengisi nama, harga, dan stok barang jualan."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(displayedProducts, key = { it.product.id }) { item ->
                        ProductItemRow(
                            item = item,
                            currencySymbol = settings.currencySymbol,
                            onEdit = { viewModel.openEditProductForm(item) },
                            onManageStock = onNavigateToInventory,
                            onDelete = { productToDelete = item }
                        )
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                    }
                }
            }
        }
    }

    // ── Fullscreen Form Tambah / Ubah Produk (Style Mockup 2) ───────────────
    if (uiState.isFormOpen) {
        Dialog(
            onDismissRequest = viewModel::closeForm,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ProductFormScreen(
                form = uiState.formState,
                categories = categories,
                currencySymbol = settings.currencySymbol,
                modifiers = modifiers,
                onFormChange = viewModel::updateForm,
                onAddVariant = viewModel::addVariant,
                onRemoveVariant = viewModel::removeVariant,
                onUpdateVariant = viewModel::updateVariant,
                onToggleModifier = viewModel::toggleModifierSelection,
                onOpenAddModifier = viewModel::openModifierDialog,
                onDismiss = viewModel::closeForm,
                onOpenAddCategory = viewModel::openCategoryDialog,
                onSave = viewModel::saveProduct
            )
        }
    }

    // ── Dialog Tambah Kategori Cepat ──────────────────────────────────────────
    if (uiState.isCategoryDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeCategoryDialog,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Kategori Baru",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                )
            },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = viewModel::updateNewCategoryName,
                    label = { Text("Nama kategori") },
                    placeholder = { Text("Contoh: Makanan & Minuman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveCategory,
                    enabled = uiState.newCategoryName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeCategoryDialog) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    // ── Dialog Tambah Topping / Modifier Cepat ───────────────────────────────
    if (uiState.isModifierDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeModifierDialog,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Tambah Topping / Modifier",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.newModifierName,
                        onValueChange = { viewModel.updateNewModifier(it, uiState.newModifierPrice) },
                        label = { Text("Nama Topping / Tambahan") },
                        placeholder = { Text("Contoh: Boba Brown Sugar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = uiState.newModifierPrice,
                        onValueChange = { viewModel.updateNewModifier(uiState.newModifierName, it.filter { ch -> ch.isDigit() }) },
                        label = { Text("Harga Tambahan (Rp)") },
                        placeholder = { Text("0 jika gratis") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveModifier,
                    enabled = uiState.newModifierName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeModifierDialog) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }

    // ── Dialog Hapus Produk ───────────────────────────────────────────────────
    productToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Hapus Produk",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                )
            },
            text = {
                Text(
                    text = "Produk \"${item.product.name}\" akan dihapus secara permanen. Tindakan ini tidak dapat dibatalkan.",
                    style = TextStyle(fontSize = 14.sp, color = Slate600)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(item.product)
                        productToDelete = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal", color = Slate700)
                }
            }
        )
    }
}

/**
 * Item baris produk di katalog sesuai mockup:
 * - 60x60dp thumbnail atau fallback ikon kategori warna-warni
 * - Nama produk SemiBold
 * - Harga tebal
 * - Stok + Chip status (Menipis / Habis)
 * - Titik tiga aksi
 */
@Composable
private fun ProductItemRow(
    item: ProductWithCategory,
    currencySymbol: String,
    onEdit: () -> Unit,
    onManageStock: () -> Unit,
    onDelete: () -> Unit
) {
    val p = item.product
    val isOutOfStock = p.trackStock && p.stock <= 0
    val isLowStock = p.trackStock && p.stock > 0 && p.stock <= p.minStock
    var menuOpen by remember { mutableStateOf(false) }
    val visual = getCategoryVisual(item.category?.name, p.name)
    val productImage = rememberProductImage(p.imageUrl)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail produk
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(visual.containerColor),
            contentAlignment = Alignment.Center
        ) {
            if (productImage != null) {
                Image(
                    bitmap = productImage,
                    contentDescription = p.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!p.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = p.imageUrl,
                    contentDescription = p.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.contentColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Informasi Produk
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = p.name,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = CurrencyFormatter.format(p.sellingPrice, currencySymbol),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Spacer(Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Stok ${p.stock.toInt()} ${p.unit}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Slate500
                    )
                )

                if (p.trackStock) {
                    if (isOutOfStock) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DangerRedContainer
                        ) {
                            Text(
                                text = "Habis",
                                color = DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isLowStock) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningAmberContainer
                        ) {
                            Text(
                                text = "Menipis",
                                color = WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Menu Aksi Tiga Titik
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Opsi",
                    tint = Slate500,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ubah produk") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                        menuOpen = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Kelola stok") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                        menuOpen = false
                        onManageStock()
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Hapus produk", color = DangerRed) },
                    leadingIcon = {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
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

/**
 * Screen Form Tambah / Ubah Produk sesuai 1:1 dengan Mockup Image 2:
 * - Top Bar: Tombol Kembali + Judul ("Tambah produk" / "Ubah produk")
 * - Kotak Foto: Kamera + "Tambah foto" / "Opsional"
 * - Nama produk *
 * - Kategori dropdown
 * - Harga jual * & Harga modal dengan box prefix [ Rp ]
 * - Kelola stok switch + Stok awal + Satuan dropdown + Batas stok menipis
 * - Accordion Pengaturan tambahan: SKU, Barcode, Varian produk
 * - Keterangan "* Wajib diisi"
 * - Tombol biru "Simpan produk"
 */
@Composable
private fun ProductFormScreen(
    form: ProductFormState,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    modifiers: List<ModifierEntity>,
    onFormChange: (ProductFormState) -> Unit,
    onAddVariant: () -> Unit,
    onRemoveVariant: (String) -> Unit,
    onUpdateVariant: (String, String, String, String) -> Unit,
    onToggleModifier: (String) -> Unit,
    onOpenAddModifier: () -> Unit,
    onDismiss: () -> Unit,
    onOpenAddCategory: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = savePickedImageToInternalStorage(context, uri)
            if (savedPath != null) {
                onFormChange(form.copy(imageUrl = savedPath))
            } else {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                onFormChange(form.copy(imageUrl = uri.toString()))
            }
        }
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var showAdvanced by rememberSaveable(form.id) { mutableStateOf(form.hasVariants || form.sku.isNotBlank() || form.barcode.isNotBlank()) }
    var isBarcodeScannerOpen by remember { mutableStateOf(false) }

    val productImage = rememberProductImage(form.imageUrl)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Slate900
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (form.name.isBlank()) "Tambah produk" else "Ubah produk",
                            style = TextStyle(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }
                    HorizontalDivider(color = Slate100, thickness = 1.dp)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ── Area Foto Produk ──────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate50)
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                            .clickable { photoPicker.launch(arrayOf("image/*")) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (productImage != null) {
                            Image(
                                bitmap = productImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (form.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = form.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Tambah foto",
                                tint = Slate600,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { photoPicker.launch(arrayOf("image/*")) }
                    ) {
                        Text(
                            text = if (form.imageUrl.isNotBlank()) "Ubah foto" else "Tambah foto",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlue
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Opsional",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        )
                    }

                    if (form.imageUrl.isNotBlank()) {
                        IconButton(onClick = { onFormChange(form.copy(imageUrl = "")) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Hapus foto",
                                tint = Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Nama Produk * ─────────────────────────────────────────────
                Row {
                    Text(
                        text = "Nama produk",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    )
                    Text(
                        text = " *",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    )
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { onFormChange(form.copy(name = it)) },
                    placeholder = {
                        Text("Ayam Bakar", style = TextStyle(fontSize = 14.sp, color = Slate400))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Slate200
                    )
                )

                Spacer(Modifier.height(16.dp))

                // ── Kategori ──────────────────────────────────────────────────
                Text(
                    text = "Kategori",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { categoryDropdownExpanded = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val selectedCat = categories.find { it.id == form.categoryId }?.name ?: "Makanan & Minuman"
                        Text(
                            text = if (form.categoryId == null) "Pilih kategori" else selectedCat,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = if (form.categoryId == null) Slate400 else Slate900,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Slate900,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pilih kategori (Tanpa kategori)") },
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
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Tambah kategori baru",
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                onOpenAddCategory()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Harga Jual * & Harga Modal (Box Prefix [ Rp ]) ────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row {
                            Text(
                                text = "Harga jual",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            )
                            Text(
                                text = " *",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        CurrencyPrefixTextField(
                            value = form.sellingPrice,
                            onValueChange = { onFormChange(form.copy(sellingPrice = it)) },
                            placeholder = "20.000"
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Harga modal",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        )
                        Spacer(Modifier.height(6.dp))
                        CurrencyPrefixTextField(
                            value = form.costPrice,
                            onValueChange = { onFormChange(form.copy(costPrice = it)) },
                            placeholder = "0"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Opsional",
                            style = TextStyle(fontSize = 12.sp, color = Slate500)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Kelola Stok ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kelola stok",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Pantau jumlah barang tersedia",
                            style = TextStyle(fontSize = 13.sp, color = Slate500)
                        )
                    }
                    Switch(
                        checked = form.trackStock,
                        onCheckedChange = { onFormChange(form.copy(trackStock = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Slate200
                        )
                    )
                }

                if (form.trackStock) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stok awal",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = form.stock,
                                onValueChange = { onFormChange(form.copy(stock = it.filter { ch -> ch.isDigit() })) },
                                placeholder = { Text("30", color = Slate400) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = Slate200
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Satuan",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { unitDropdownExpanded = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = form.unit.ifBlank { "pcs" },
                                        style = TextStyle(fontSize = 15.sp, color = Slate900)
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Slate900,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = unitDropdownExpanded,
                                    onDismissRequest = { unitDropdownExpanded = false }
                                ) {
                                    listOf("pcs", "porsi", "botol", "kg", "karung", "bungkus", "paket", "cup", "kotak").forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text(u) },
                                            onClick = {
                                                onFormChange(form.copy(unit = u))
                                                unitDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Batas stok menipis",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = form.minStock,
                        onValueChange = { onFormChange(form.copy(minStock = it.filter { ch -> ch.isDigit() })) },
                        placeholder = { Text("5", color = Slate400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Peringatan saat stok ${form.minStock.ifBlank { "5" }} ${form.unit} atau kurang.",
                        style = TextStyle(fontSize = 12.sp, color = Slate500)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Pengaturan Tambahan (Accordion) ───────────────────────────
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAdvanced = !showAdvanced },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengaturan tambahan",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "SKU, barcode, dan varian produk",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = Slate500
                                    )
                                )
                            }
                            Icon(
                                imageVector = if (showAdvanced) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (showAdvanced) {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(Modifier.height(14.dp))

                            // SKU
                            Text(
                                text = "SKU",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = form.sku,
                                onValueChange = { onFormChange(form.copy(sku = it)) },
                                placeholder = { Text("Contoh: AYM-BKR-01", color = Slate400) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(Modifier.height(14.dp))

                            // Barcode
                            Text(
                                text = "Barcode",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = form.barcode,
                                onValueChange = { onFormChange(form.copy(barcode = it)) },
                                placeholder = { Text("Scan atau masukkan barcode", color = Slate400) },
                                trailingIcon = {
                                    IconButton(onClick = { isBarcodeScannerOpen = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.QrCodeScanner,
                                            contentDescription = "Scan barcode dengan kamera",
                                            tint = BrandBlue
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            // Varian Produk
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Varian produk",
                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    )
                                    Text(
                                        text = "Ukuran, level rasa, atau pilihan",
                                        style = TextStyle(fontSize = 12.sp, color = Slate500)
                                    )
                                }
                                Switch(
                                    checked = form.hasVariants,
                                    onCheckedChange = { checked ->
                                        if (checked && form.variants.isEmpty()) {
                                            onAddVariant()
                                        } else {
                                            onFormChange(form.copy(hasVariants = checked))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandBlue
                                    )
                                )
                            }

                            if (form.hasVariants) {
                                Spacer(Modifier.height(10.dp))
                                form.variants.forEachIndexed { idx, v ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Slate50),
                                        border = BorderStroke(1.dp, Slate200)
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Varian ${idx + 1}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrandBlue
                                                )
                                                IconButton(
                                                    onClick = { onRemoveVariant(v.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.DeleteOutline,
                                                        contentDescription = "Hapus",
                                                        tint = DangerRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            OutlinedTextField(
                                                value = v.name,
                                                onValueChange = { onUpdateVariant(v.id, it, v.price, v.stock) },
                                                placeholder = { Text("Nama varian (mis. Regular / Jumbo)") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = v.price,
                                                    onValueChange = {
                                                        onUpdateVariant(
                                                            v.id,
                                                            v.name,
                                                            it.filter { ch -> ch.isDigit() },
                                                            v.stock
                                                        )
                                                    },
                                                    placeholder = { Text("Harga jual") },
                                                    prefix = { Text("Rp ") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                if (form.trackStock) {
                                                    OutlinedTextField(
                                                        value = v.stock,
                                                        onValueChange = {
                                                            onUpdateVariant(
                                                                v.id,
                                                                v.name,
                                                                v.price,
                                                                it.filter { ch -> ch.isDigit() }
                                                            )
                                                        },
                                                        placeholder = { Text("Stok") },
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

                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = onAddVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate200)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = BrandBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Tambah varian",
                                        color = BrandBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Slate200, thickness = 1.dp)
                            Spacer(Modifier.height(16.dp))

                            // ── Topping & Modifier Tambahan (F&B) ────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Topping & Tambahan (Modifier)",
                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                    )
                                    Text(
                                        text = "Pilihan topping saat kasir memilih menu ini",
                                        style = TextStyle(fontSize = 12.sp, color = Slate500)
                                    )
                                }
                                TextButton(onClick = onOpenAddModifier) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = BrandBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "+ Baru",
                                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            if (modifiers.isEmpty()) {
                                Text(
                                    text = "Belum ada pilihan topping. Buat dengan klik tombol '+ Baru'.",
                                    style = TextStyle(fontSize = 12.sp, color = Slate400)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    modifiers.forEach { mod ->
                                        val isSelected = form.selectedModifierIds.contains(mod.id)
                                        Surface(
                                            onClick = { onToggleModifier(mod.id) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) BrandBlue.copy(alpha = 0.1f) else Slate50,
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) BrandBlue else Slate200
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = BrandBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                }
                                                Column {
                                                    Text(
                                                        text = mod.name,
                                                        style = TextStyle(
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) BrandBlue else Slate900
                                                        )
                                                    )
                                                    Text(
                                                        text = if (mod.price > 0) "+${CurrencyFormatter.format(mod.price, currencySymbol)}" else "Gratis / Opsi",
                                                        style = TextStyle(
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) BrandBlue else Slate500
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Catatan Wajib Diisi ────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "*",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Wajib diisi",
                        style = TextStyle(fontSize = 12.sp, color = Slate500)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Tombol Simpan Produk ──────────────────────────────────────
                Button(
                    onClick = onSave,
                    enabled = form.name.isNotBlank() && form.sellingPrice.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Simpan produk",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        if (isBarcodeScannerOpen) {
            BarcodeScannerInputDialog(
                title = "Scan Barcode Produk",
                onBarcodeScanned = { scannedCode ->
                    onFormChange(form.copy(barcode = scannedCode))
                    isBarcodeScannerOpen = false
                },
                onDismiss = { isBarcodeScannerOpen = false }
            )
        }
    }
}

/**
 * Textfield dengan prefix box [ Rp ] di sebelah kiri seperti di mockup:
 * - Sisi kiri kotak abu-abu dengan tulisan 'Rp'
 * - Sisi kanan input angka dengan formatting ribuan
 */
@Composable
private fun CurrencyPrefixTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0"
) {
    val numericValue = value.filter { it.isDigit() }
    val displayValue = if (numericValue.isEmpty()) "" else {
        try {
            java.text.NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(numericValue.toLong())
        } catch (_: Exception) {
            numericValue
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [ Rp ] Prefix Box
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Slate50),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rp",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                )
                // Border pembatas kanan
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Slate200)
                )
            }

            // Input field
            BasicTextField(
                value = displayValue,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    onValueChange(digits)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (displayValue.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Slate400
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

private data class CategoryVisual(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

private fun getCategoryVisual(categoryName: String?, productName: String = ""): CategoryVisual {
    val cat = (categoryName ?: "").lowercase()
    val prod = productName.lowercase()
    val combined = "$cat $prod"

    return when {
        // Minuman / Drink / Coffee / Tea
        combined.contains("kopi") || combined.contains("coffee") || combined.contains("cafe") -> {
            CategoryVisual(
                icon = Icons.Outlined.Coffee,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
            )
        }
        combined.contains("minum") || combined.contains("drink") || combined.contains("teh") ||
                combined.contains("jus") || combined.contains("juice") || combined.contains("susu") ||
                combined.contains("air") || combined.contains("boba") || combined.contains("es ") || combined.contains("syrup") -> {
            CategoryVisual(
                icon = Icons.Outlined.LocalDrink,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0284C7)
            )
        }
        // Sembako / Bahan Pokok
        combined.contains("sembako") || combined.contains("beras") || combined.contains("minyak") ||
                combined.contains("gula") || combined.contains("telur") || combined.contains("terigu") ||
                combined.contains("tepung") || combined.contains("garam") || combined.contains("pokok") ||
                combined.contains("bumbu") -> {
            CategoryVisual(
                icon = Icons.Outlined.ShoppingBag,
                containerColor = Color(0xFFFEF9C3),
                contentColor = Color(0xFFCA8A04)
            )
        }
        // Camilan / Snack / Kue / Roti
        combined.contains("camil") || combined.contains("snack") || combined.contains("keripik") ||
                combined.contains("kue") || combined.contains("roti") || combined.contains("biskuit") ||
                combined.contains("wafer") || combined.contains("permen") || combined.contains("cokelat") -> {
            CategoryVisual(
                icon = Icons.Outlined.BakeryDining,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFEA580C)
            )
        }
        // Makanan
        combined.contains("makan") || combined.contains("food") || combined.contains("ayam") ||
                combined.contains("nasi") || combined.contains("mie") || combined.contains("bakso") ||
                combined.contains("soto") || combined.contains("goreng") || combined.contains("bakar") -> {
            CategoryVisual(
                icon = Icons.Outlined.Fastfood,
                containerColor = Color(0xFFFFE4E6),
                contentColor = Color(0xFFE11D48)
            )
        }
        else -> {
            CategoryVisual(
                icon = Icons.Outlined.Inventory2,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun rememberProductImage(imageUrl: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(imageUrl) {
        if (imageUrl.isNullOrBlank()) null
        else {
            try {
                if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                    val uri = Uri.parse(imageUrl)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } else {
                    val file = File(imageUrl)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun savePickedImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "product_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
