package com.rising.pos.feature.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.model.CartState
import com.rising.pos.ui.components.quantityLabel
import com.rising.pos.ui.components.CashierIcons
import com.rising.pos.ui.theme.CashierTheme
import com.rising.pos.ui.theme.posMoney
import com.rising.pos.ui.theme.Slate100
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.rising.pos.ui.theme.PosTextStyles
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.customer.CustomerFormState
import com.rising.pos.feature.customer.components.CustomerFormDialog
import com.rising.pos.feature.customer.components.CustomerPickerDialog
import com.rising.pos.feature.pos.components.CartView
import com.rising.pos.feature.pos.components.CheckoutDialog
import com.rising.pos.feature.pos.components.DiscountDialog
import com.rising.pos.feature.pos.components.HeldOrdersDialog
import com.rising.pos.feature.pos.components.HoldCartDialog
import com.rising.pos.feature.pos.components.ProductCard
import com.rising.pos.feature.pos.components.ProductPhoto
import com.rising.pos.feature.pos.components.ReceiptSuccessDialog
import com.rising.pos.feature.pos.components.TableFloorDialog
import com.rising.pos.feature.pos.components.CameraBarcodeScannerDialog
import com.rising.pos.feature.pos.components.VariantPickerDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.components.PosHeaderTitleSection
import com.rising.pos.ui.components.PosHeaderActionButton
import com.rising.pos.ui.components.PosSearchBar
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Sell
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate50
import com.rising.pos.ui.theme.DangerRed
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.PrimaryBlue

private val BrandBlue = PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) = CashierTheme { PosScreenContent(viewModel) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosScreenContent(viewModel: PosViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val heldTransactions by viewModel.heldTransactions.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val tables by viewModel.tables.collectAsState()

    val isTableEnabled = settings.isTableEnabled || settings.type == com.rising.pos.core.model.BusinessType.CAFE
    val occupiedTablesCount = tables.count { it.isOccupied }

    var newCustomerFormState by remember { mutableStateOf(CustomerFormState()) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val isCompactHeight = screenHeight < 500
    val isTablet = screenWidth >= 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isTablet) {
            PosTabletContent(
                settings = settings,
                products = products,
                categories = categories,
                cart = uiState.cart,
                searchQuery = uiState.searchQuery,
                selectedCategoryId = uiState.selectedCategoryId,
                heldOrdersCount = heldTransactions.size,
                occupiedTablesCount = occupiedTablesCount,
                isTableEnabled = isTableEnabled,
                onSearchChange = viewModel::updateSearchQuery,
                onScanBarcode = viewModel::scanBarcode,
                onOpenScanner = { isCameraScannerOpen = true },
                onOpenHeldOrders = viewModel::openHeldOrdersList,
                onOpenTables = { viewModel.openTableFloorDialog(isPickerMode = false) },
                onSelectCategory = viewModel::selectCategory,
                onProductClick = viewModel::onProductClicked,
                onDecreaseProduct = viewModel::decreaseProductQuantity,
                onUpdateQuantity = viewModel::updateQuantity,
                onRemoveItem = viewModel::removeItem,
                onClearCart = viewModel::clearCart,
                onCheckout = viewModel::openCheckoutDialog,
                onHoldCart = viewModel::openHoldDialog,
                onOpenCustomerPicker = { viewModel.openCustomerPicker(true) },
                onRemoveCustomer = { viewModel.setCustomer(null) },
                onOpenTablePicker = { viewModel.openTableFloorDialog(isPickerMode = true) },
                onRemoveTable = { viewModel.setTableNumber(null) },
                onOpenDiscountDialog = { viewModel.openDiscountDialog(true) },
                compact = isCompactHeight
            )
        } else {
            PosPhoneCatalog(
                settings = settings,
                products = products,
                categories = categories,
                cart = uiState.cart,
                searchQuery = uiState.searchQuery,
                selectedCategoryId = uiState.selectedCategoryId,
                heldOrdersCount = heldTransactions.size,
                occupiedTablesCount = occupiedTablesCount,
                isTableEnabled = isTableEnabled,
                onSearch = viewModel::updateSearchQuery,
                onScanBarcode = viewModel::scanBarcode,
                onOpenScanner = { isCameraScannerOpen = true },
                onHeldOrders = viewModel::openHeldOrdersList,
                onOpenTables = { viewModel.openTableFloorDialog(isPickerMode = false) },
                onCategory = viewModel::selectCategory,
                onProduct = viewModel::onProductClicked,
                onDecrease = viewModel::decreaseProductQuantity,
                onCart = { viewModel.setCartSheetOpen(true) },
                onCheckout = viewModel::openCheckoutDialog
            )
        }

        // ── Modal Bottom Sheet untuk Detail Keranjang ───────────────────────
        if (uiState.isCartSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setCartSheetOpen(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CartView(
                    cart = uiState.cart,
                    settings = settings,
                    onUpdateQuantity = viewModel::updateQuantity,
                    onRemoveItem = viewModel::removeItem,
                    onClearCart = viewModel::clearCart,
                    onCheckout = {
                        viewModel.setCartSheetOpen(false)
                        viewModel.openCheckoutDialog()
                    },
                    onHoldCart = {
                        viewModel.setCartSheetOpen(false)
                        viewModel.openHoldDialog()
                    },
                    onOpenCustomerPicker = { viewModel.openCustomerPicker(true) },
                    onRemoveCustomer = { viewModel.setCustomer(null) },
                    onOpenTablePicker = { viewModel.openTableFloorDialog(isPickerMode = true) },
                    onRemoveTable = { viewModel.setTableNumber(null) },
                    onOpenDiscountDialog = { viewModel.openDiscountDialog(true) },
                    onAddProducts = { viewModel.setCartSheetOpen(false) },
                    compact = true
                )
            }
        }

        // ── Dialogs Pendukung ───────────────────────────────────────────────
        if (uiState.isCheckoutDialogOpen) {
            CheckoutDialog(
                cart = uiState.cart,
                settings = settings,
                isProcessing = uiState.isProcessingPayment,
                errorMessage = uiState.paymentErrorMessage,
                tables = tables,
                onDismiss = viewModel::closeCheckoutDialog,
                onConfirmPayment = { method, paid, orderType, note, splitMethod, splitAmount ->
                    viewModel.processPayment(method, paid, orderType, note, splitMethod, splitAmount)
                }
            )
        }

        if (uiState.isHoldDialogOpen) {
            HoldCartDialog(
                itemCount = uiState.cart.totalItemCount.toInt(),
                tables = tables,
                preselectedTable = uiState.cart.tableNumber,
                onDismiss = viewModel::closeHoldDialog,
                onConfirmHold = viewModel::holdCurrentCart
            )
        }

        if (uiState.isHeldOrdersListDialogOpen) {
            HeldOrdersDialog(
                heldOrders = heldTransactions,
                currencySymbol = settings.currencySymbol,
                onDismiss = viewModel::closeHeldOrdersList,
                onResumeOrder = viewModel::resumeHeldTransaction,
                onDeleteOrder = viewModel::deleteHeldOrder
            )
        }

        if (uiState.isTableFloorDialogOpen) {
            TableFloorDialog(
                tables = tables,
                heldOrders = heldTransactions,
                selectedTableNumber = uiState.cart.tableNumber,
                isSelectionMode = uiState.isTablePickerMode,
                currencySymbol = settings.currencySymbol,
                onDismiss = viewModel::closeTableFloorDialog,
                onSelectTable = viewModel::setTableNumber,
                onResumeHeldOrder = viewModel::resumeHeldTransaction,
                onSaveTable = viewModel::saveTable,
                onDeleteTable = viewModel::deleteTable,
                onToggleOccupied = viewModel::toggleTableOccupied,
                onSeedDefaultTables = viewModel::seedDefaultTables
            )
        }

        if (uiState.isCustomerPickerOpen) {
            CustomerPickerDialog(
                customers = customers,
                selectedCustomer = uiState.cart.customer,
                onSelectCustomer = { cust -> viewModel.setCustomer(cust) },
                onAddNewCustomer = {
                    newCustomerFormState = CustomerFormState(isOpen = true)
                    viewModel.openNewCustomerForm(true)
                },
                onDismiss = { viewModel.openCustomerPicker(false) }
            )
        }

        if (uiState.isNewCustomerFormOpen && newCustomerFormState.isOpen) {
            CustomerFormDialog(
                formState = newCustomerFormState,
                onNameChange = { newCustomerFormState = newCustomerFormState.copy(name = it, errorMessage = null) },
                onPhoneChange = { newCustomerFormState = newCustomerFormState.copy(phone = it) },
                onEmailChange = { newCustomerFormState = newCustomerFormState.copy(email = it) },
                onAddressChange = { newCustomerFormState = newCustomerFormState.copy(address = it) },
                onNotesChange = { newCustomerFormState = newCustomerFormState.copy(notes = it) },
                onDismiss = {
                    newCustomerFormState = CustomerFormState(isOpen = false)
                    viewModel.openNewCustomerForm(false)
                },
                onSave = {
                    if (newCustomerFormState.name.trim().isBlank()) {
                        newCustomerFormState = newCustomerFormState.copy(errorMessage = "Nama pelanggan wajib diisi")
                    } else {
                        viewModel.createCustomerAndSelect(
                            name = newCustomerFormState.name.trim(),
                            phone = newCustomerFormState.phone.trim(),
                            email = newCustomerFormState.email.trim(),
                            address = newCustomerFormState.address.trim(),
                            notes = newCustomerFormState.notes.trim()
                        )
                        newCustomerFormState = CustomerFormState(isOpen = false)
                    }
                }
            )
        }

        if (uiState.isDiscountDialogOpen) {
            DiscountDialog(
                subtotal = uiState.cart.subtotal,
                currencySymbol = settings.currencySymbol,
                currentDiscount = uiState.cart.discount,
                currentReason = uiState.cart.discountReason,
                onDismiss = { viewModel.openDiscountDialog(false) },
                onApplyDiscount = { amount, reason ->
                    viewModel.applyDiscount(amount, reason)
                }
            )
        }

        uiState.lastCompletedTransaction?.let { completedTrx ->
            ReceiptSuccessDialog(
                transactionWithDetails = completedTrx,
                settings = settings,
                isPrinting = uiState.isPrinting,
                printMessage = uiState.printMessage,
                printErrorMessage = uiState.printErrorMessage,
                onPrintReceipt = { viewModel.printReceipt(completedTrx) },
                onDismiss = viewModel::dismissSuccessDialog
            )
        }

        uiState.selectedProductForVariants?.let { product ->
            VariantPickerDialog(
                product = product,
                variants = uiState.availableVariants,
                modifiers = uiState.availableModifiers,
                currencySymbol = settings.currencySymbol,
                onConfirm = { variant, selectedModifiers ->
                    viewModel.addProductWithCustomizationsToCart(product, variant, selectedModifiers)
                },
                onDismiss = viewModel::dismissVariantPicker
            )
        }

        if (isCameraScannerOpen) {
            CameraBarcodeScannerDialog(
                cart = uiState.cart,
                currencySymbol = settings.currencySymbol,
                onProcessScan = viewModel::processBarcodeScan,
                onUpdateQuantity = viewModel::updateQuantity,
                onDismiss = { isCameraScannerOpen = false }
            )
        }
    }
}

@Composable
internal fun PosHeader(
    storeName: String,
    cashierName: String,
    searchQuery: String,
    heldOrdersCount: Int = 0,
    occupiedTablesCount: Int = 0,
    isTableEnabled: Boolean = false,
    onSearchChange: (String) -> Unit,
    onOpenScanner: () -> Unit = {},
    onScanBarcode: (String) -> Unit = {},
    onOpenHeldOrders: () -> Unit = {},
    onOpenTables: () -> Unit = {},
    showBarcodeScanner: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Baris Header: Judul "Kasir" + Subtitle Toko + Tombol Aksi Rounded
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.rising.pos.ui.components.PosHeaderTitleSection(
                title = "Kasir",
                subtitle = storeName.ifBlank { "Rising Studio" },
                modifier = Modifier.weight(1f)
            )

            if (isTableEnabled) {
                com.rising.pos.ui.components.PosHeaderActionButton(
                    icon = Icons.Outlined.TableRestaurant,
                    contentDescription = "Kelola Meja",
                    onClick = onOpenTables,
                    badgeCount = occupiedTablesCount,
                    badgeColor = Color(0xFFEA580C)
                )
                Spacer(Modifier.width(8.dp))
            }

            com.rising.pos.ui.components.PosHeaderActionButton(
                icon = CashierIcons.Receipt,
                contentDescription = "Pesanan Tertunda",
                onClick = onOpenHeldOrders,
                badgeCount = heldOrdersCount,
                badgeColor = MaterialTheme.colorScheme.primary
            )
        }

        // Search Bar dengan Ikon Barcode Terpadu
        com.rising.pos.ui.components.PosSearchBar(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Cari nama atau kode barang",
            trailingBarcodeAction = if (showBarcodeScanner) onOpenScanner else null,
            onSearchAction = { if (showBarcodeScanner) onScanBarcode(searchQuery) }
        )
    }
}

@Composable
private fun CategoryFilters(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
    edgePadding: Dp = 16.dp
) {
    val selectedTabIndex = remember(selectedCategoryId, categories) {
        if (selectedCategoryId == null) 0
        else {
            val idx = categories.indexOfFirst { it.id == selectedCategoryId }
            if (idx >= 0) idx + 1 else 0
        }
    }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = BrandBlue,
        edgePadding = edgePadding,
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
        divider = { HorizontalDivider(color = Slate100) },
        modifier = Modifier.fillMaxWidth()
    ) {
        // Tab "Semua"
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onSelectCategory(null) },
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
                onClick = { onSelectCategory(cat.id) },
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
}

@Composable
private fun ProductGrid(
    products: List<ProductWithCategory>,
    currencySymbol: String,
    cartItems: List<com.rising.pos.domain.model.CartItem>,
    onProductClick: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    onDecreaseProduct: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    columns: Int,
    isFiltered: Boolean,
    stockTrackingEnabled: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            WorkspaceEmptyState(
                icon = if (isFiltered) Icons.Default.Search else Icons.Outlined.Inventory2,
                title = if (isFiltered) "Produk tidak ditemukan" else "Belum ada produk",
                description = if (isFiltered) {
                    "Coba kata kunci lain atau pilih kategori Semua."
                } else {
                    "Tambahkan barang jualan lewat menu Produk, lalu produk akan muncul di sini."
                }
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            contentPadding = PaddingValues(top = 2.dp, bottom = if (compact) 6.dp else 12.dp),
            modifier = modifier
        ) {
            items(products, key = { it.product.id }) { item ->
                val cartQuantity = cartItems
                    .filter { it.product.id == item.product.id }
                    .sumOf { it.quantity }

                ProductCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    cartQuantity = cartQuantity,
                    onIncrease = { onProductClick(item.product) },
                    onDecrease = { onDecreaseProduct(item.product) },
                    onClick = { onProductClick(item.product) },
                    stockTrackingEnabled = stockTrackingEnabled,
                    compact = compact
                )
            }
        }
    }
}

/** Stateless catalog shared with the visual and interaction tests. */
@Composable
internal fun PosPhoneCatalog(
    settings: BusinessSettings, products: List<ProductWithCategory>, categories: List<CategoryEntity>, cart: CartState,
    searchQuery: String, selectedCategoryId: String?, heldOrdersCount: Int,
    occupiedTablesCount: Int = 0, isTableEnabled: Boolean = false,
    onSearch: (String) -> Unit, onScanBarcode: (String) -> Unit = {}, onOpenScanner: () -> Unit,
    onHeldOrders: () -> Unit, onOpenTables: () -> Unit = {},
    onCategory: (String?) -> Unit, onProduct: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    onDecrease: (com.rising.pos.core.database.entity.ProductEntity) -> Unit, onCart: () -> Unit, onCheckout: () -> Unit
) = CashierTheme {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.weight(1f).padding(top = 12.dp)) {
            Box(Modifier.padding(horizontal = 16.dp)) {
                PosHeader(
                    settings.name, settings.cashierName, searchQuery, heldOrdersCount,
                    occupiedTablesCount = occupiedTablesCount, isTableEnabled = isTableEnabled,
                    onSearchChange = onSearch, onOpenScanner = onOpenScanner, onScanBarcode = onScanBarcode,
                    onOpenHeldOrders = onHeldOrders, onOpenTables = onOpenTables,
                    showBarcodeScanner = settings.isBarcodeEnabled
                )
            }
            Spacer(Modifier.height(8.dp))
            CategoryFilters(categories, selectedCategoryId, onCategory)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                ProductGrid(products, settings.currencySymbol, cart.items, onProduct, onDecrease, 2,
                    searchQuery.isNotBlank() || selectedCategoryId != null,
                    stockTrackingEnabled = settings.isStockTrackingEnabled, modifier = Modifier.fillMaxSize())
            }
        }
        if (cart.items.isNotEmpty()) {
            val calc = cart.calculateTotals(settings.isTaxEnabled, settings.taxPercentage, settings.isTaxInclusive,
                settings.isServiceChargeEnabled, settings.serviceChargePercentage)
            val total = posMoney(calc.grandTotal, settings.currencySymbol)
            HorizontalDivider()
            Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth().clickable(onClick = onCart).heightIn(min = 50.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Keranjang", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(10.dp)); Text("${quantityLabel(cart.totalItemCount)} barang", fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                        Text(cart.items.joinToString(", ") { "${quantityLabel(it.quantity)} ${it.product.name}" },
                            fontSize = 11.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.KeyboardArrowUp, "Lihat keranjang", tint = colors.onSurfaceVariant)
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text("Total", Modifier.weight(1f), fontSize = 15.sp)
                    Text(total, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Button(onCheckout, Modifier.fillMaxWidth().heightIn(min = 44.dp), shape = RoundedCornerShape(6.dp)) {
                    Text("Bayar $total", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(10.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(22.dp))
                }
            }
        }
    }
}

/**
 * Tampilan khusus tablet / 2-pane dengan layout 2 kolom (Katalog kiri, Keranjang kanan)
 * yang otomatis adaptif untuk Tablet maupun HP Landscape (compact height).
 */
@Composable
internal fun PosTabletContent(
    settings: BusinessSettings,
    products: List<ProductWithCategory>,
    categories: List<CategoryEntity>,
    cart: CartState,
    searchQuery: String,
    selectedCategoryId: String?,
    heldOrdersCount: Int,
    occupiedTablesCount: Int,
    isTableEnabled: Boolean,
    onSearchChange: (String) -> Unit,
    onScanBarcode: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenHeldOrders: () -> Unit,
    onOpenTables: () -> Unit,
    onSelectCategory: (String?) -> Unit,
    onProductClick: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    onDecreaseProduct: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onHoldCart: () -> Unit,
    onOpenCustomerPicker: () -> Unit,
    onRemoveCustomer: () -> Unit,
    onOpenTablePicker: () -> Unit,
    onRemoveTable: () -> Unit,
    onOpenDiscountDialog: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) = CashierTheme {
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val calc = cart.calculateTotals(
        settings.isTaxEnabled,
        settings.taxPercentage,
        settings.isTaxInclusive,
        settings.isServiceChargeEnabled,
        settings.serviceChargePercentage
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── 1. Top Header Full Width ──────────────────────────────────────────
        if (compact) {
            // HP Landscape Compact Header: Satu baris ramping (Judul + Search Bar + Aksi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kasir",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(Modifier.width(10.dp))

                PosSearchBar(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Cari nama / kode...",
                    trailingBarcodeAction = if (settings.isBarcodeEnabled) onOpenScanner else null,
                    onSearchAction = { if (settings.isBarcodeEnabled) onScanBarcode(searchQuery) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                )

                Spacer(Modifier.width(8.dp))

                if (isTableEnabled) {
                    PosHeaderActionButton(
                        icon = Icons.Outlined.TableRestaurant,
                        contentDescription = "Kelola Meja",
                        onClick = onOpenTables,
                        badgeCount = occupiedTablesCount,
                        badgeColor = Color(0xFFEA580C),
                        size = 38.dp
                    )
                    Spacer(Modifier.width(6.dp))
                }

                PosHeaderActionButton(
                    icon = CashierIcons.Receipt,
                    contentDescription = "Pesanan Tertunda",
                    onClick = onOpenHeldOrders,
                    badgeCount = heldOrdersCount,
                    badgeColor = MaterialTheme.colorScheme.primary,
                    size = 38.dp
                )
            }
        } else {
            // Tablet Full-Width Header Luas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PosHeaderTitleSection(
                    title = "Kasir",
                    subtitle = settings.name.ifBlank { "Rising Studio" },
                    modifier = Modifier.weight(1f)
                )

                if (isTableEnabled) {
                    PosHeaderActionButton(
                        icon = Icons.Outlined.TableRestaurant,
                        contentDescription = "Kelola Meja",
                        onClick = onOpenTables,
                        badgeCount = occupiedTablesCount,
                        badgeColor = Color(0xFFEA580C)
                    )
                    Spacer(Modifier.width(10.dp))
                }

                PosHeaderActionButton(
                    icon = CashierIcons.Receipt,
                    contentDescription = "Pesanan Tertunda",
                    onClick = onOpenHeldOrders,
                    badgeCount = heldOrdersCount,
                    badgeColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(color = Slate200, thickness = 0.8.dp)

        // ── 2. Split Body (Katalog Kiri & Keranjang Kanan) ────────────────────
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // ── Kolom Kiri: Katalog Produk ────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(if (compact) 1.4f else 1.5f)
                    .fillMaxHeight()
                    .padding(
                        start = if (compact) 14.dp else 20.dp,
                        end = if (compact) 10.dp else 16.dp,
                        top = if (compact) 6.dp else 14.dp
                    )
            ) {
                // Search Bar hanya jika bukan compact (karena pada compact sudah di top bar)
                if (!compact) {
                    PosSearchBar(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = "Cari nama atau kode barang",
                        trailingBarcodeAction = if (settings.isBarcodeEnabled) onOpenScanner else null,
                        onSearchAction = { if (settings.isBarcodeEnabled) onScanBarcode(searchQuery) }
                    )
                    Spacer(Modifier.height(14.dp))
                }

                // Tab Kategori
                CategoryFilters(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = onSelectCategory,
                    edgePadding = 0.dp
                )

                Spacer(Modifier.height(if (compact) 6.dp else 12.dp))

                // Grid Produk 3 Kolom
                ProductGrid(
                    products = products,
                    currencySymbol = settings.currencySymbol,
                    cartItems = cart.items,
                    onProductClick = onProductClick,
                    onDecreaseProduct = onDecreaseProduct,
                    columns = 3,
                    isFiltered = searchQuery.isNotBlank() || selectedCategoryId != null,
                    stockTrackingEnabled = settings.isStockTrackingEnabled,
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
            }

            // Pemisah Garis Vertikal Antar Kolom
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Slate200)
            )

            // ── Kolom Kanan: Panel Keranjang Tablet / Landscape ───────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = if (compact) 12.dp else 20.dp,
                        end = if (compact) 14.dp else 20.dp,
                        top = if (compact) 6.dp else 14.dp,
                        bottom = if (compact) 8.dp else 12.dp
                    )
            ) {
                // Header Keranjang + Tombol Kosongkan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (compact) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Keranjang",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "(${quantityLabel(cart.totalItemCount)})",
                                fontSize = 11.5.sp,
                                color = Slate500
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Keranjang",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${quantityLabel(cart.totalItemCount)} barang • ${cart.items.size} produk",
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        }
                    }

                    if (cart.items.isNotEmpty()) {
                        TextButton(
                            onClick = { confirmClear = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Kosongkan",
                                color = DangerRed,
                                fontSize = if (compact) 12.sp else 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(if (compact) 6.dp else 14.dp))

                // Kotak Pemilih Pelanggan
                if (compact) {
                    Surface(
                        onClick = onOpenCustomerPicker,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate200),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = CashierIcons.Person,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = cart.customer?.name ?: "Pilih pelanggan (Opsional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (cart.customer != null) {
                                IconButton(
                                    onClick = onRemoveCustomer,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Lepas pelanggan",
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = onOpenCustomerPicker,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate200),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Slate50,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CashierIcons.Person,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cart.customer?.name ?: "Pilih pelanggan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    text = if (cart.customer == null) "Opsional" else cart.customer.phone.orEmpty().ifBlank { "Terpilih" },
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }

                            if (cart.customer != null) {
                                IconButton(
                                    onClick = onRemoveCustomer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Lepas pelanggan",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Kotak Meja (Jika F&B aktif)
                if (isTableEnabled) {
                    Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
                    if (compact) {
                        Surface(
                            onClick = onOpenTablePicker,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate200),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TableRestaurant,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (cart.tableNumber.isNullOrBlank()) "Pilih Meja (Opsional)" else "Meja ${cart.tableNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!cart.tableNumber.isNullOrBlank()) {
                                    IconButton(
                                        onClick = onRemoveTable,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Lepas meja",
                                            tint = Slate400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            onClick = onOpenTablePicker,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Slate200),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Slate50,
                                    border = BorderStroke(1.dp, Slate200),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.TableRestaurant,
                                            contentDescription = null,
                                            tint = Slate500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (cart.tableNumber.isNullOrBlank()) "Pilih Meja" else cart.tableNumber!!,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Spacer(Modifier.height(1.dp))
                                    Text(
                                        text = if (cart.tableNumber.isNullOrBlank()) "Dine-in / Meja (Opsional)" else "Makan di tempat",
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }

                                if (!cart.tableNumber.isNullOrBlank()) {
                                    IconButton(
                                        onClick = onRemoveTable,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Lepas meja",
                                            tint = Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(if (compact) 6.dp else 10.dp))

                // Daftar Item Keranjang (Scrollable)
                if (cart.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = CashierIcons.Box,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(if (compact) 36.dp else 44.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Keranjang masih kosong",
                                fontSize = if (compact) 12.5.sp else 14.sp,
                                color = Slate400
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(cart.items, key = { it.cartItemId }) { item ->
                            TabletCartItemRow(
                                item = item,
                                currencySymbol = settings.currencySymbol,
                                onUpdateQuantity = onUpdateQuantity,
                                onRemoveItem = onRemoveItem,
                                compact = compact
                            )
                        }
                    }
                }

                // Bagian Bawah Keranjang (Pinned Summary & Tombol Aksi)
                if (cart.items.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (compact) 4.dp else 8.dp)
                    ) {
                        HorizontalDivider(color = Slate200, thickness = 0.8.dp)
                        Spacer(Modifier.height(if (compact) 4.dp else 10.dp))

                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subtotal",
                                fontSize = if (compact) 12.5.sp else 14.sp,
                                color = Slate600
                            )
                            Text(
                                text = posMoney(calc.subtotal, settings.currencySymbol),
                                fontSize = if (compact) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(Modifier.height(if (compact) 4.dp else 8.dp))

                        // Tambah diskon
                        Surface(
                            onClick = onOpenDiscountDialog,
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (compact) 1.dp else 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sell,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(if (compact) 15.dp else 18.dp)
                                )
                                Spacer(Modifier.width(if (compact) 6.dp else 8.dp))
                                Text(
                                    text = if (calc.discount > 0) "Diskon" else "Tambah diskon",
                                    fontSize = if (compact) 12.sp else 14.sp,
                                    color = Slate700,
                                    modifier = Modifier.weight(1f)
                                )
                                if (calc.discount > 0) {
                                    Text(
                                        text = "−${posMoney(calc.discount, settings.currencySymbol)}",
                                        fontSize = if (compact) 12.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(if (compact) 15.dp else 18.dp)
                                )
                            }
                        }

                        if (!cart.discountReason.isNullOrBlank()) {
                            Text(
                                text = cart.discountReason,
                                fontSize = if (compact) 10.5.sp else 11.5.sp,
                                color = Slate500,
                                modifier = Modifier.padding(start = if (compact) 20.dp else 26.dp)
                            )
                        }

                        if (calc.serviceCharge > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Biaya layanan", fontSize = if (compact) 12.sp else 14.sp, color = Slate600)
                                Text(posMoney(calc.serviceCharge, settings.currencySymbol), fontSize = if (compact) 12.5.sp else 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }

                        if (settings.isTaxEnabled && calc.tax > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (settings.isTaxInclusive) "Pajak (termasuk)" else "Pajak", fontSize = if (compact) 12.sp else 14.sp, color = Slate600)
                                Text(posMoney(calc.tax, settings.currencySymbol), fontSize = if (compact) 12.5.sp else 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }

                        Spacer(Modifier.height(if (compact) 6.dp else 10.dp))

                        // Total bayar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total bayar",
                                fontSize = if (compact) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = posMoney(calc.grandTotal, settings.currencySymbol),
                                fontSize = if (compact) 18.sp else 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                        }

                        Spacer(Modifier.height(if (compact) 8.dp else 14.dp))

                        // Tombol [ || Tahan ] dan [ Bayar Rp... > ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onHoldCart,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (compact) 38.dp else 48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, PrimaryBlue),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                                    tint = PrimaryBlue
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Tahan",
                                    fontSize = if (compact) 13.sp else 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                            }

                            Button(
                                onClick = onCheckout,
                                modifier = Modifier
                                    .weight(1.85f)
                                    .height(if (compact) 38.dp else 48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Bayar ${posMoney(calc.grandTotal, settings.currencySymbol)}",
                                    fontSize = if (compact) 13.5.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Kosongkan keranjang?") },
            text = { Text("Semua barang di pesanan ini akan dihapus dari keranjang.") },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClearCart() }) {
                    Text("Kosongkan", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun TabletCartItemRow(
    item: com.rising.pos.domain.model.CartItem,
    currencySymbol: String,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Foto Produk
        ProductPhoto(
            imageUrl = item.product.imageUrl,
            name = item.product.name,
            modifier = Modifier
                .size(if (compact) 44.dp else 64.dp)
                .clip(RoundedCornerShape(if (compact) 6.dp else 8.dp))
        )

        Spacer(Modifier.width(if (compact) 8.dp else 12.dp))

        // Detail Produk, Harga, Stepper & Tombol Hapus
        Column(modifier = Modifier.weight(1f)) {
            // Baris 1: Nama Produk & Total Harga
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        fontSize = if (compact) 13.sp else 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.variant?.let {
                        Text(
                            text = it.name,
                            fontSize = if (compact) 10.5.sp else 11.5.sp,
                            color = Slate500
                        )
                    }
                    if (item.selectedModifiers.isNotEmpty()) {
                        Text(
                            text = "+ " + item.selectedModifiers.joinToString(", ") { it.name },
                            fontSize = if (compact) 10.sp else 11.sp,
                            color = PrimaryBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = posMoney(item.totalPrice, currencySymbol),
                    fontSize = if (compact) 13.5.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Spacer(Modifier.height(1.dp))

            // Baris 2: Harga Satuan / Unit
            Text(
                text = "${posMoney(item.unitPrice, currencySymbol)} / ${item.product.unit.ifBlank { "pcs" }}",
                fontSize = if (compact) 11.sp else 12.sp,
                color = Slate500
            )

            Spacer(Modifier.height(if (compact) 4.dp else 6.dp))

            // Baris 3: Stepper [-] qty [+] di kiri, Tempat Sampah di kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Container
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.height(if (compact) 26.dp else 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        IconButton(
                            onClick = { onUpdateQuantity(item.cartItemId, -1.0) },
                            enabled = item.quantity > 1,
                            modifier = Modifier.size(if (compact) 24.dp else 28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Kurang",
                                tint = if (item.quantity > 1) PrimaryBlue else Slate400,
                                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                            )
                        }

                        Text(
                            text = quantityLabel(item.quantity),
                            fontSize = if (compact) 12.sp else 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 10.dp)
                        )

                        IconButton(
                            onClick = { onUpdateQuantity(item.cartItemId, 1.0) },
                            modifier = Modifier.size(if (compact) 24.dp else 28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Tombol Hapus Tempat Sampah
                IconButton(
                    onClick = { onRemoveItem(item.cartItemId) },
                    modifier = Modifier.size(if (compact) 26.dp else 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus ${item.product.name}",
                        tint = Slate400,
                        modifier = Modifier.size(if (compact) 17.dp else 20.dp)
                    )
                }
            }
        }
    }
}


