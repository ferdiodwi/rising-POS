package com.rising.pos.feature.pos

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import com.rising.pos.ui.components.WorkspaceEmptyState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.util.CurrencyFormatter
import androidx.compose.material.icons.filled.PauseCircle
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
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
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
            // ── Tablet Layout (Side-by-side 3 pane / 2 pane) ──────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                // Main Product Catalog Pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    PosHeader(
                        storeName = settings.name,
                        deviceId = settings.deviceId,
                        searchQuery = uiState.searchQuery,
                        heldOrdersCount = heldTransactions.size,
                        onSearchChange = viewModel::updateSearchQuery,
                        onScanBarcode = viewModel::scanBarcode,
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories Bar
                    CategoriesRow(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Products Grid
                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
                        columns = 3,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right Cart Sidebar
                Card(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
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

            // ── Phone Layout (Vertical + Sticky Bottom Cart Bar) ─────────────
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    PosHeader(
                        storeName = settings.name,
                        deviceId = settings.deviceId,
                        searchQuery = uiState.searchQuery,
                        heldOrdersCount = heldTransactions.size,
                        onSearchChange = viewModel::updateSearchQuery,
                        onScanBarcode = viewModel::scanBarcode,
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoriesRow(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
                        columns = 2,
                        modifier = Modifier
                            .weight(1f)

                    )
                }

                // Sticky Bottom Cart Summary Bar
                if (uiState.cart.items.isNotEmpty()) {
                    val calc = uiState.cart.calculateTotals(
                        isTaxEnabled = settings.isTaxEnabled,
                        taxPercentage = settings.taxPercentage,
                        isTaxInclusive = settings.isTaxInclusive,
                        isServiceChargeEnabled = settings.isServiceChargeEnabled,
                        serviceChargePercentage = settings.serviceChargePercentage
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                            .clickable { viewModel.setCartSheetOpen(true) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.16f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "${uiState.cart.totalItemCount.toInt()} Item",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "Lihat pesanan",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Phone Cart Bottom Sheet
            if (uiState.isCartSheetOpen) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.setCartSheetOpen(false) },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
        }

        // Checkout Dialog
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

        // Hold Cart Dialog
        if (uiState.isHoldDialogOpen) {
            HoldCartDialog(
                itemCount = uiState.cart.totalItemCount.toInt(),
                onDismiss = viewModel::closeHoldDialog,
                onConfirmHold = viewModel::holdCurrentCart
            )
        }

        // Held Orders List Dialog
        if (uiState.isHeldOrdersListDialogOpen) {
            HeldOrdersDialog(
                heldOrders = heldTransactions,
                currencySymbol = settings.currencySymbol,
                onDismiss = viewModel::closeHeldOrdersList,
                onResumeOrder = viewModel::resumeHeldTransaction,
                onDeleteOrder = viewModel::deleteHeldOrder
            )
        }

        // Customer Picker Dialog
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

        // Quick New Customer Dialog from POS
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

        // Discount Dialog
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

        // Receipt Success Dialog
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

        // Variant Picker Dialog
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
    deviceId: String,
    searchQuery: String,
    heldOrdersCount: Int = 0,
    onSearchChange: (String) -> Unit,
    onScanBarcode: (String) -> Unit = {},
    onOpenHeldOrders: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kasir", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    storeName.ifBlank { "Toko saya" } + " · " + deviceId,
                    style = MaterialTheme.typography.bodySmall, color = Slate500,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (heldOrdersCount > 0) {
                TextButton(onClick = onOpenHeldOrders) {
                    Icon(Icons.Default.PauseCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tertunda ($heldOrdersCount)")
                }
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari produk atau scan barcode") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Default.Clear, "Hapus pencarian", tint = Slate500)
                } }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onScanBarcode(searchQuery) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Slate200,
                focusedBorderColor = PrimaryBlue
            )
        )
    }
}

@Composable
private fun CategoriesRow(
    categories: List<com.rising.pos.core.database.entity.CategoryEntity>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Semua") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat.id) },
                label = { Text(cat.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<com.rising.pos.core.database.entity.ProductWithCategory>,
    currencySymbol: String,
    onProductClick: (com.rising.pos.core.database.entity.ProductEntity) -> Unit,
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
                icon = Icons.Default.Search,
                title = if (isFiltered) "Produk tidak ditemukan" else "Siap mulai berjualan?",
                description = if (isFiltered) "Coba kata kunci lain atau pilih kategori Semua."
                    else "Tambahkan barang jualan lewat menu Produk. Setelah itu, pilih produk di sini untuk membuat pesanan."
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (columns == 3) 160.dp else 145.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = modifier
        ) {
            items(products, key = { it.product.id }) { item ->
                ProductCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onClick = { onProductClick(item.product) }
                )
            }
        }
    }
}
