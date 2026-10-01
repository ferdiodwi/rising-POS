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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.rising.pos.ui.theme.PosTextStyles
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate900
import com.rising.pos.ui.theme.SuccessGreen

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
                        deviceId = settings.deviceId,
                        searchQuery = uiState.searchQuery,
                        heldOrdersCount = heldTransactions.size,
                        onSearchChange = viewModel::updateSearchQuery,
                        onScanBarcode = viewModel::scanBarcode,
                        onOpenHeldOrders = viewModel::openHeldOrdersList
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoriesRow(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )
                    Spacer(Modifier.height(12.dp))
                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
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
            // ── HP: katalog vertikal + bar keranjang di dasar ────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
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
                    Spacer(Modifier.height(12.dp))
                    CategoriesRow(
                        categories = categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )
                    Spacer(Modifier.height(12.dp))
                    ProductGrid(
                        products = products,
                        currencySymbol = settings.currencySymbol,
                        isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null,
                        onProductClick = viewModel::onProductClicked,
                        columns = 2,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bar keranjang: satu-satunya aksen besar di layar ini.
                if (uiState.cart.items.isNotEmpty()) {
                    val calc = uiState.cart.calculateTotals(
                        isTaxEnabled = settings.isTaxEnabled,
                        taxPercentage = settings.taxPercentage,
                        isTaxInclusive = settings.isTaxInclusive,
                        isServiceChargeEnabled = settings.isServiceChargeEnabled,
                        serviceChargePercentage = settings.serviceChargePercentage
                    )

                    Surface(
                        onClick = { viewModel.setCartSheetOpen(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 16.dp)
                            .heightIn(min = 64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryBlue,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${uiState.cart.totalItemCount.toInt()} item di keranjang",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(calc.grandTotal, settings.currencySymbol),
                                    style = PosTextStyles.priceCard,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Lihat pesanan",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

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
    deviceId: String,
    searchQuery: String,
    heldOrdersCount: Int = 0,
    onSearchChange: (String) -> Unit,
    onScanBarcode: (String) -> Unit = {},
    onOpenHeldOrders: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kasir", style = MaterialTheme.typography.headlineSmall, color = Slate900)
                Text(
                    text = listOf(storeName.ifBlank { "Toko saya" }, deviceId)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Hanya muncul saat ada pesanan tertunda: tidak ada tombol tanpa isi.
            if (heldOrdersCount > 0) {
                Surface(
                    onClick = onOpenHeldOrders,
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$heldOrdersCount tertunda",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari nama, SKU, atau barcode") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = Slate500)
                    }
                }
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
                focusedBorderColor = MaterialTheme.colorScheme.primary
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
            CategoryChip(
                label = "Semua",
                selected = selectedCategoryId == null,
                onClick = { onSelectCategory(null) }
            )
        }
        items(categories, key = { it.id }) { cat ->
            CategoryChip(
                label = cat.name,
                selected = selectedCategoryId == cat.id,
                onClick = { onSelectCategory(cat.id) }
            )
        }
    }
}

/**
 * Chip kategori. Terpilih = aksen navy (bg tint + border navy), tidak terpilih =
 * permukaan netral. Tinggi 40dp agar nyaman di-tap.
 */
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun ProductGrid(
    products: List<ProductWithCategory>,
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
            columns = GridCells.Adaptive(if (columns == 3) 160.dp else 145.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
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
