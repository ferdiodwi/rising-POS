package com.rising.pos.feature.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
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
import com.rising.pos.feature.pos.components.VariantPickerDialog
import com.rising.pos.ui.components.WorkspaceEmptyState
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate600
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val heldTransactions by viewModel.heldTransactions.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var newCustomerFormState by remember { mutableStateOf(CustomerFormState()) }

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
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoryUnderlineTabs(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
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
            // ── HP: Header, Search Bar, Underline Tabs, Product Grid, Bottom Cart Bar ──
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                ) {
                    PosHeader(
                        storeName = settings.name,
                        cashierName = settings.cashierName,
                        searchQuery = uiState.searchQuery,
                        heldOrdersCount = heldTransactions.size,
                        onSearchChange = viewModel::updateSearchQuery,
                        onScanBarcode = viewModel::scanBarcode,
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoryUnderlineTabs(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )
                    Spacer(Modifier.height(8.dp))
                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        cartItems = uiState.cart.items,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
                        onDecreaseProduct = viewModel::decreaseProductQuantity,
                        columns = 2,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Bar Keranjang Bawah Modern Sesuai Mockup ──────────────────
                if (uiState.cart.items.isNotEmpty()) {
                    val calc = uiState.cart.calculateTotals(
                        isTaxEnabled = settings.isTaxEnabled,
                        taxPercentage = settings.taxPercentage,
                        isTaxInclusive = settings.isTaxInclusive,
                        isServiceChargeEnabled = settings.isServiceChargeEnabled,
                        serviceChargePercentage = settings.serviceChargePercentage
                    )
                    val totalItemCount = uiState.cart.totalItemCount.toInt()
                    val itemSummary = uiState.cart.items.joinToString(", ") {
                        "${it.quantity.toInt()} ${it.product.name}"
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            // Baris 1: Keranjang  3 barang               ^
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setCartSheetOpen(true) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Keranjang",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$totalItemCount barang",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = Slate500
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Buka Keranjang",
                                    tint = Slate600,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Baris 2: Ringkasan item
                            Text(
                                text = itemSummary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )

                            // Baris 3: Total                             Rp27.000
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Baris 4: Tombol Bayar
                            Button(
                                onClick = viewModel::openCheckoutDialog,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Bayar ${CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                    onOpenDiscountDialog = { viewModel.openDiscountDialog(true) }
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
    }
}

@Composable
private fun PosHeader(
    storeName: String,
    cashierName: String,
    searchQuery: String,
    heldOrdersCount: Int = 0,
    onSearchChange: (String) -> Unit,
    onScanBarcode: (String) -> Unit = {},
    onOpenHeldOrders: () -> Unit = {}
) {
    var showBarcodeDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Baris Header: Ikon Toko + Nama Usaha & Kasir + Tombol Struk/Pesanan Tertunda
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName.ifBlank { "Warung Bu Siti" },
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
                modifier = Modifier.size(40.dp)
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
                        imageVector = Icons.Outlined.ReceiptLong,
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
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier.size(36.dp)
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
                    onClick = { showBarcodeDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = Slate700,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showBarcodeDialog) {
        var barcodeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBarcodeDialog = false },
            title = {
                Text(
                    text = "Scan / Masukkan Barcode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan kode barcode atau SKU produk untuk ditambahkan ke keranjang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        label = { Text("Kode Barcode") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (barcodeInput.isNotBlank()) {
                            onScanBarcode(barcodeInput.trim())
                        }
                        showBarcodeDialog = false
                    }
                ) {
                    Text("Cari & Tambah", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBarcodeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CategoryUnderlineTabs(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit
) {
    val selectedIndex = if (selectedCategoryId == null) 0 else {
        val idx = categories.indexOfFirst { it.id == selectedCategoryId }
        if (idx >= 0) idx + 1 else 0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(color = Slate200, thickness = 1.dp)
            },
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            Tab(
                selected = selectedCategoryId == null,
                onClick = { onSelectCategory(null) },
                text = {
                    Text(
                        text = "Semua",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = if (selectedCategoryId == null) MaterialTheme.colorScheme.primary else Slate600
                    )
                }
            )
            categories.forEach { category ->
                val isSelected = selectedCategoryId == category.id
                Tab(
                    selected = isSelected,
                    onClick = { onSelectCategory(category.id) },
                    text = {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Slate600
                        )
                    }
                )
            }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            modifier = modifier
        ) {
            items(products, key = { it.product.id }) { item ->
                val cartQuantity = cartItems
                    .filter { it.product.id == item.product.id }
                    .sumOf { it.quantity }
                    .toInt()

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
