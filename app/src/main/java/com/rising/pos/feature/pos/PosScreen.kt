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
import com.rising.pos.feature.pos.components.ReceiptSuccessDialog
import com.rising.pos.feature.pos.components.CameraBarcodeScannerDialog
import com.rising.pos.feature.pos.components.VariantPickerDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700

private val BrandBlue = Color(0xFF2563EB)

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

    var newCustomerFormState by remember { mutableStateOf(CustomerFormState()) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 840

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isTablet) {
            // ── Tablet: katalog kiri, keranjang kanan ────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    PosHeader(
                        storeName = settings.name,
                        cashierName = settings.cashierName,
                        searchQuery = uiState.searchQuery,
                        heldOrdersCount = heldTransactions.size,
                        onSearchChange = viewModel::updateSearchQuery,
                        onScanBarcode = viewModel::scanBarcode,
                        onOpenScanner = { isCameraScannerOpen = true },
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoryFilters(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory,
                        edgePadding = 0.dp
                    )
                    Spacer(Modifier.height(10.dp))
                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        cartItems = uiState.cart.items,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
                        onDecreaseProduct = viewModel::decreaseProductQuantity,
                        columns = 3,
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    CartView(
                        cart = uiState.cart,
                        settings = settings,
                        onUpdateQuantity = viewModel::updateQuantity,
                        onRemoveItem = viewModel::removeItem,
                        onClearCart = viewModel::clearCart,
                        onCheckout = viewModel::openCheckoutDialog,
                        onHoldCart = viewModel::openHoldDialog,
                        onOpenCustomerPicker = { viewModel.openCustomerPicker(true) },
                        onRemoveCustomer = { viewModel.setCustomer(null) },
                        onOpenDiscountDialog = { viewModel.openDiscountDialog(true) }
                    )
                }
            }
        } else {
            PosPhoneCatalog(
                settings = settings,
                products = products,
                categories = categories,
                cart = uiState.cart,
                searchQuery = uiState.searchQuery,
                selectedCategoryId = uiState.selectedCategoryId,
                heldOrdersCount = heldTransactions.size,
                onSearch = viewModel::updateSearchQuery,
                onScanBarcode = viewModel::scanBarcode,
                onOpenScanner = { isCameraScannerOpen = true },
                onHeldOrders = viewModel::openHeldOrdersList,
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
                onDismiss = viewModel::closeCheckoutDialog,
                onConfirmPayment = viewModel::processPayment
            )
        }

        if (uiState.isHoldDialogOpen) {
            HoldCartDialog(
                itemCount = uiState.cart.totalItemCount.toInt(),
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
                currencySymbol = settings.currencySymbol,
                onSelectVariant = { variant ->
                    viewModel.addProductVariantToCart(product, variant)
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
    onSearchChange: (String) -> Unit,
    onOpenScanner: () -> Unit = {},
    onScanBarcode: (String) -> Unit = {},
    onOpenHeldOrders: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Baris Header: Ikon Toko + Nama Usaha & Kasir + Tombol Struk/Pesanan Tertunda
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = CashierIcons.Store,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName.ifBlank { "Nama usaha" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cashierName.ifBlank { "Kasir" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOpenHeldOrders,
                modifier = Modifier.size(48.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (heldOrdersCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) {
                                Text("$heldOrdersCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = CashierIcons.Receipt,
                        contentDescription = "Pesanan Tertunda",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Search Bar dengan Ikon Barcode Terpadu
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, Slate200)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onScanBarcode(searchQuery) }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari nama atau kode barang",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate400,
                                    fontSize = 14.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.6f)
                        .background(Slate200)
                )
                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = CashierIcons.Barcode,
                        contentDescription = "Scan barcode",
                        tint = Slate700,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 2.dp, bottom = 12.dp),
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
                    onClick = { onProductClick(item.product) }
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
    onSearch: (String) -> Unit, onScanBarcode: (String) -> Unit = {}, onOpenScanner: () -> Unit, onHeldOrders: () -> Unit,
    onCategory: (String?) -> Unit, onProduct: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
    onDecrease: (com.rising.pos.core.database.entity.ProductEntity) -> Unit, onCart: () -> Unit, onCheckout: () -> Unit
) = CashierTheme {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.weight(1f).padding(top = 8.dp)) {
            Box(Modifier.padding(horizontal = 16.dp)) {
                PosHeader(
                    settings.name, settings.cashierName, searchQuery, heldOrdersCount, onSearch,
                    onOpenScanner = onOpenScanner, onScanBarcode = onScanBarcode, onOpenHeldOrders = onHeldOrders
                )
            }
            Spacer(Modifier.height(8.dp))
            CategoryFilters(categories, selectedCategoryId, onCategory)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                ProductGrid(products, settings.currencySymbol, cart.items, onProduct, onDecrease, 2,
                    searchQuery.isNotBlank() || selectedCategoryId != null, Modifier.fillMaxSize())
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
