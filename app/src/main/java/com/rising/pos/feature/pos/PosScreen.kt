package com.rising.pos.feature.pos

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.feature.pos.components.CartView
import com.rising.pos.feature.pos.components.CheckoutDialog
import com.rising.pos.feature.pos.components.ProductCard
import com.rising.pos.feature.pos.components.ReceiptSuccessDialog
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
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

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

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
                        onSearchChange = viewModel::updateSearchQuery
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
                        onProductClick = viewModel::addToCart,
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    CartView(
                        cart = uiState.cart,
                        settings = settings,
                        onUpdateQuantity = viewModel::updateQuantity,
                        onRemoveItem = viewModel::removeItem,
                        onClearCart = viewModel::clearCart,
                        onCheckout = viewModel::openCheckoutDialog
                    )
                }
            }
        } else {
            // ── Phone Layout (Vertical + Sticky Bottom Cart Bar) ─────────────
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    PosHeader(
                        storeName = settings.name,
                        deviceId = settings.deviceId,
                        searchQuery = uiState.searchQuery,
                        onSearchChange = viewModel::updateSearchQuery
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
                        onProductClick = viewModel::addToCart,
                        columns = 2,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (uiState.cart.items.isNotEmpty()) 72.dp else 0.dp)
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
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { viewModel.setCartSheetOpen(true) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SuccessGreen, CircleShape),
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
                                Column {
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
                                color = SuccessGreen
                            ) {
                                Text(
                                    text = "Lihat Keranjang",
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
                        onCheckout = viewModel::openCheckoutDialog
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

        // Receipt Success Dialog
        uiState.lastCompletedTransaction?.let { completedTrx ->
            ReceiptSuccessDialog(
                transactionWithDetails = completedTrx,
                settings = settings,
                onDismiss = viewModel::dismissSuccessDialog
            )
        }
    }
}

@Composable
private fun PosHeader(
    storeName: String,
    deviceId: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "POS $deviceId",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari produk...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500, modifier = Modifier.size(16.dp))
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier
                .width(220.dp)
                .height(46.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
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
                    selectedLabelColor = PrimaryBlue
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
                    selectedLabelColor = PrimaryBlue
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
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada produk. Tambahkan produk di menu Produk.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
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
