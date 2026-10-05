package com.rising.pos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.domain.repository.CustomerRepository
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.TableRepository
import com.rising.pos.domain.repository.TransactionRepository
import com.rising.pos.core.printer.BluetoothPrinterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PosUiState(
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val cart: CartState = CartState(),
    val isCheckoutDialogOpen: Boolean = false,
    val isCartSheetOpen: Boolean = false,
    val isHoldDialogOpen: Boolean = false,
    val isHeldOrdersListDialogOpen: Boolean = false,
    val isCustomerPickerOpen: Boolean = false,
    val isNewCustomerFormOpen: Boolean = false,
    val isDiscountDialogOpen: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val paymentErrorMessage: String? = null,
    val lastCompletedTransaction: TransactionWithDetails? = null,
    val isPrinting: Boolean = false,
    val printMessage: String? = null,
    val printErrorMessage: String? = null,
    val selectedProductForVariants: ProductEntity? = null,
    val availableVariants: List<ProductVariantEntity> = emptyList(),
    val availableModifiers: List<ModifierEntity> = emptyList(),
    val isTableFloorDialogOpen: Boolean = false,
    val isTablePickerMode: Boolean = false
)

sealed interface ScanFeedback {
    data class Success(
        val cartItemId: String,
        val productName: String,
        val price: Long,
        val quantity: Double,
        val imageUrl: String? = null
    ) : ScanFeedback
    data class NotFound(val barcode: String) : ScanFeedback
    data class NeedsVariant(val product: ProductEntity, val variants: List<ProductVariantEntity>) : ScanFeedback
}

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository,
    private val tableRepository: TableRepository,
    private val appPreferences: AppPreferences,
    private val printerManager: BluetoothPrinterManager
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val customers: StateFlow<List<CustomerEntity>> = customerRepository.getAllCustomers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<CategoryEntity>> = productRepository.getActiveCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val heldTransactions: StateFlow<List<TransactionWithDetails>> =
        transactionRepository.getHeldTransactions().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tables: StateFlow<List<RestaurantTableEntity>> =
        tableRepository.getAllTables().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState = _uiState.asStateFlow()


    // Combine products with active filters
    val filteredProducts: StateFlow<List<ProductWithCategory>> = combine(
        productRepository.getActiveProducts(),
        _uiState
    ) { allProducts, state ->
        allProducts.filter { item ->
            val matchCategory = state.selectedCategoryId == null || item.product.categoryId == state.selectedCategoryId
            val matchSearch = state.searchQuery.isBlank() ||
                    item.product.name.contains(state.searchQuery, ignoreCase = true) ||
                    (item.product.barcode != null && item.product.barcode.contains(state.searchQuery, ignoreCase = true))
            matchCategory && matchSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    suspend fun processBarcodeScan(barcode: String): ScanFeedback {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return ScanFeedback.NotFound(barcode)

        // 1. Cek kecocokan barcode varian
        val variant = productRepository.getVariantByBarcode(trimmed)
        if (variant != null) {
            val parent = productRepository.getProductById(variant.productId)
            if (parent != null && parent.isActive) {
                addProductVariantToCart(parent, variant)
                val finalPrice = parent.sellingPrice + variant.priceAdjustment
                val foundItem = _uiState.value.cart.items
                    .find { it.product.id == parent.id && it.variant?.id == variant.id }
                val qty = foundItem?.quantity ?: 1.0
                _uiState.update { it.copy(searchQuery = "") }
                return ScanFeedback.Success(
                    cartItemId = foundItem?.cartItemId ?: "",
                    productName = "${parent.name} - ${variant.name}",
                    price = finalPrice,
                    quantity = qty,
                    imageUrl = parent.imageUrl
                )
            }
        }

        // 2. Cek kecocokan produk utama (berdasarkan barcode atau SKU)
        val matched = productRepository.getProductByBarcode(trimmed)
        if (matched != null) {
            val variants = productRepository.getVariantsByProductId(matched.product.id)
            val modifiers = productRepository.getModifiersByProductId(matched.product.id)
            if ((variants.isNotEmpty() || modifiers.isNotEmpty()) && settings.value.isModifierEnabled) {
                _uiState.update {
                    it.copy(
                        selectedProductForVariants = matched.product,
                        availableVariants = variants,
                        availableModifiers = modifiers
                    )
                }
                return ScanFeedback.NeedsVariant(matched.product, variants)
            } else {
                addToCart(matched.product)
                val foundItem = _uiState.value.cart.items
                    .find { it.product.id == matched.product.id && it.variant == null }
                val qty = foundItem?.quantity ?: 1.0
                _uiState.update { it.copy(searchQuery = "") }
                return ScanFeedback.Success(
                    cartItemId = foundItem?.cartItemId ?: "",
                    productName = matched.product.name,
                    price = matched.product.sellingPrice,
                    quantity = qty,
                    imageUrl = matched.product.imageUrl
                )
            }
        }

        return ScanFeedback.NotFound(trimmed)
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val result = processBarcodeScan(barcode)
            if (result is ScanFeedback.NotFound) {
                updateSearchQuery(barcode.trim())
            }
        }
    }

    fun onProductClicked(product: ProductEntity) {
        viewModelScope.launch {
            // Bila "Topping & Modifier" nonaktif, produk bervarian langsung masuk
            // keranjang (pakai harga dasar) tanpa memunculkan pemilih varian.
            if (!settings.value.isModifierEnabled) {
                addToCart(product)
                return@launch
            }
            val variants = productRepository.getVariantsByProductId(product.id)
            val modifiers = productRepository.getModifiersByProductId(product.id)
            if (variants.isNotEmpty() || modifiers.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        selectedProductForVariants = product,
                        availableVariants = variants,
                        availableModifiers = modifiers
                    )
                }
            } else {
                addToCart(product)
            }
        }
    }

    fun addProductVariantToCart(product: ProductEntity, variant: ProductVariantEntity) {
        addProductWithCustomizationsToCart(product, variant, emptyList())
    }

    fun addProductWithCustomizationsToCart(
        product: ProductEntity,
        variant: ProductVariantEntity?,
        modifiers: List<ModifierEntity>
    ) {
        _uiState.update { state ->
            val modIds = modifiers.map { it.id }.toSet()
            val existingItemIndex = state.cart.items.indexOfFirst {
                it.product.id == product.id &&
                it.variant?.id == variant?.id &&
                it.selectedModifiers.map { m -> m.id }.toSet() == modIds
            }

            val updatedItems = if (existingItemIndex != -1) {
                state.cart.items.mapIndexed { index, item ->
                    if (index == existingItemIndex) {
                        item.copy(quantity = item.quantity + 1)
                    } else item
                }
            } else {
                state.cart.items + CartItem(
                    product = product,
                    variant = variant,
                    selectedModifiers = modifiers,
                    quantity = 1.0
                )
            }

            state.copy(
                cart = state.cart.copy(items = updatedItems),
                selectedProductForVariants = null,
                availableVariants = emptyList(),
                availableModifiers = emptyList()
            )
        }
    }

    fun dismissVariantPicker() {
        _uiState.update {
            it.copy(
                selectedProductForVariants = null,
                availableVariants = emptyList(),
                availableModifiers = emptyList()
            )
        }
    }

    fun addToCart(product: ProductEntity) {
        _uiState.update { state ->
            val existingItemIndex = state.cart.items.indexOfFirst {
                it.product.id == product.id && it.variant == null && it.selectedModifiers.isEmpty()
            }

            val updatedItems = if (existingItemIndex != -1) {
                state.cart.items.mapIndexed { index, item ->
                    if (index == existingItemIndex) {
                        item.copy(quantity = item.quantity + 1)
                    } else item
                }
            } else {
                state.cart.items + CartItem(
                    product = product,
                    quantity = 1.0
                )
            }

            state.copy(cart = state.cart.copy(items = updatedItems))
        }
    }

    fun decreaseProductQuantity(product: ProductEntity) {
        _uiState.update { state ->
            val existingIndex = state.cart.items.indexOfLast {
                it.product.id == product.id && it.selectedModifiers.isEmpty()
            }
            if (existingIndex == -1) return@update state

            val existingItem = state.cart.items[existingIndex]
            val newQty = existingItem.quantity - 1.0
            val updatedItems = if (newQty > 0) {
                state.cart.items.mapIndexed { idx, item ->
                    if (idx == existingIndex) item.copy(quantity = newQty) else item
                }
            } else {
                state.cart.items.filterIndexed { idx, _ -> idx != existingIndex }
            }
            state.copy(cart = state.cart.copy(items = updatedItems))
        }
    }

    fun updateQuantity(cartItemId: String, delta: Double) {
        _uiState.update { state ->
            val updatedItems = state.cart.items.mapNotNull { item ->
                if (item.cartItemId == cartItemId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
            state.copy(cart = state.cart.copy(items = updatedItems))
        }
    }

    fun removeItem(cartItemId: String) {
        _uiState.update { state ->
            val updatedItems = state.cart.items.filterNot { it.cartItemId == cartItemId }
            state.copy(cart = state.cart.copy(items = updatedItems))
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = CartState()) }
    }

    fun setOrderType(orderType: OrderType) {
        _uiState.update { it.copy(cart = it.cart.copy(orderType = orderType)) }
    }

    fun setCustomer(customer: CustomerEntity?) {
        _uiState.update { it.copy(cart = it.cart.copy(customer = customer), isCustomerPickerOpen = false) }
    }

    fun openCustomerPicker(isOpen: Boolean) {
        _uiState.update { it.copy(isCustomerPickerOpen = isOpen) }
    }

    fun openNewCustomerForm(isOpen: Boolean) {
        _uiState.update { it.copy(isNewCustomerFormOpen = isOpen) }
    }

    fun createCustomerAndSelect(name: String, phone: String?, email: String? = null, address: String? = null, notes: String? = null) {
        viewModelScope.launch {
            val newCustomer = CustomerEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone?.takeIf { it.isNotBlank() },
                email = email?.takeIf { it.isNotBlank() },
                address = address?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() }
            )
            customerRepository.saveCustomer(newCustomer).onSuccess {
                setCustomer(newCustomer)
                _uiState.update { it.copy(isNewCustomerFormOpen = false, isCustomerPickerOpen = false) }
            }
        }
    }

    fun applyDiscount(amount: Long, reason: String?) {
        _uiState.update { it.copy(cart = it.cart.copy(discount = amount, discountReason = reason)) }
    }

    fun openDiscountDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isDiscountDialogOpen = isOpen) }
    }

    fun openCheckoutDialog() {
        if (_uiState.value.cart.items.isNotEmpty()) {
            _uiState.update { it.copy(isCheckoutDialogOpen = true, paymentErrorMessage = null) }
        }
    }

    fun closeCheckoutDialog() {
        _uiState.update { it.copy(isCheckoutDialogOpen = false, paymentErrorMessage = null) }
    }

    fun setCartSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCartSheetOpen = isOpen) }
    }

    fun processPayment(
        paymentMethod: PaymentMethod,
        cashPaidAmount: Long,
        orderType: OrderType = _uiState.value.cart.orderType,
        note: String? = _uiState.value.cart.note,
        splitPaymentMethod: PaymentMethod? = null,
        splitAmount: Long = 0L
    ) {
        val currentSettings = settings.value
        val currentCart = _uiState.value.cart.copy(
            orderType = orderType,
            note = if (!note.isNullOrBlank()) note else _uiState.value.cart.note
        )
        val calc = currentCart.calculateTotals(
            isTaxEnabled = currentSettings.isTaxEnabled,
            taxPercentage = currentSettings.taxPercentage,
            isTaxInclusive = currentSettings.isTaxInclusive,
            isServiceChargeEnabled = currentSettings.isServiceChargeEnabled,
            serviceChargePercentage = currentSettings.serviceChargePercentage
        )

        val paidAmount = if (splitPaymentMethod != null) {
            cashPaidAmount
        } else if (paymentMethod == PaymentMethod.CASH) {
            cashPaidAmount
        } else {
            calc.grandTotal
        }

        _uiState.update { it.copy(isProcessingPayment = true, paymentErrorMessage = null) }

        viewModelScope.launch {
            val result = transactionRepository.processCheckout(
                cartState = currentCart,
                paymentMethod = paymentMethod,
                paymentAmount = paidAmount,
                deviceId = currentSettings.deviceId,
                cashierId = currentSettings.cashierName,
                isTaxEnabled = currentSettings.isTaxEnabled,
                taxPercentage = currentSettings.taxPercentage,
                isTaxInclusive = currentSettings.isTaxInclusive,
                isServiceChargeEnabled = currentSettings.isServiceChargeEnabled,
                serviceChargePercentage = currentSettings.serviceChargePercentage,
                isStockTrackingEnabled = currentSettings.isStockTrackingEnabled,
                splitPaymentMethod = splitPaymentMethod,
                splitAmount = splitAmount
            )

            result.fold(
                onSuccess = { completedTrx ->
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            isCheckoutDialogOpen = false,
                            isCartSheetOpen = false,
                            cart = CartState(), // Clear cart
                            lastCompletedTransaction = completedTrx,
                            printMessage = null,
                            printErrorMessage = null
                        )
                    }

                    if (currentSettings.autoPrintReceipt && currentSettings.printerMacAddress.isNotBlank()) {
                        printReceipt(completedTrx)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            paymentErrorMessage = error.localizedMessage ?: "Terjadi kesalahan saat checkout"
                        )
                    }
                }
            )
        }
    }

    fun printReceipt(transactionWithDetails: TransactionWithDetails? = _uiState.value.lastCompletedTransaction) {
        val trx = transactionWithDetails ?: return
        val currentSettings = settings.value
        if (currentSettings.printerMacAddress.isBlank()) {
            _uiState.update { it.copy(printErrorMessage = "Printer belum dipilih di Pengaturan.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printErrorMessage = null, printMessage = null) }
            val result = printerManager.printReceipt(
                macAddress = currentSettings.printerMacAddress,
                transactionWithDetails = trx,
                settings = currentSettings
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isPrinting = false,
                            printMessage = "Struk berhasil dicetak!"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isPrinting = false,
                            printErrorMessage = err.localizedMessage ?: "Gagal mencetak struk."
                        )
                    }
                }
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update {
            it.copy(
                lastCompletedTransaction = null,
                isPrinting = false,
                printMessage = null,
                printErrorMessage = null
            )
        }
    }

    fun openHoldDialog() {
        if (_uiState.value.cart.items.isNotEmpty()) {
            _uiState.update { it.copy(isHoldDialogOpen = true) }
        }
    }

    fun closeHoldDialog() {
        _uiState.update { it.copy(isHoldDialogOpen = false) }
    }

    fun openHeldOrdersList() {
        _uiState.update { it.copy(isHeldOrdersListDialogOpen = true) }
    }

    fun closeHeldOrdersList() {
        _uiState.update { it.copy(isHeldOrdersListDialogOpen = false) }
    }

    fun holdCurrentCart(note: String) {
        val currentSettings = settings.value
        val currentCart = _uiState.value.cart
        if (currentCart.items.isEmpty()) return

        viewModelScope.launch {
            val result = transactionRepository.holdTransaction(
                cartState = currentCart,
                deviceId = currentSettings.deviceId,
                note = note,
                cashierId = currentSettings.cashierName
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        cart = CartState(),
                        isHoldDialogOpen = false,
                        isCartSheetOpen = false
                    )
                }
            }
        }
    }

    fun resumeHeldTransaction(heldTrx: TransactionWithDetails) {
        viewModelScope.launch {
            val restoredCartItems = mutableListOf<CartItem>()
            for (itemDetail in heldTrx.items) {
                val item = itemDetail.item
                val existingProduct = productRepository.getProductById(item.productId) ?: ProductEntity(
                    id = item.productId,
                    name = item.productName,
                    sellingPrice = item.unitPrice,
                    costPrice = 0L,
                    stock = 0.0
                )
                val modifiers = itemDetail.modifiers.map { mod ->
                    ModifierEntity(
                        id = mod.modifierId,
                        name = mod.modifierName,
                        price = mod.price
                    )
                }
                restoredCartItems.add(
                    CartItem(
                        product = existingProduct,
                        quantity = item.qty,
                        selectedModifiers = modifiers,
                        discount = item.discount,
                        note = item.note
                    )
                )
            }

            val tableFromNote = heldTrx.transaction.note?.let { n ->
                if (n.contains("Meja: ")) n.substringAfter("Meja: ").substringBefore("\n").substringBefore(",").trim()
                else if (n.startsWith("Meja ", ignoreCase = true)) n.trim()
                else null
            }

            _uiState.update {
                it.copy(
                    cart = CartState(
                        items = restoredCartItems,
                        discount = heldTrx.transaction.discount,
                        discountReason = heldTrx.transaction.discountReason,
                        orderType = heldTrx.transaction.orderType,
                        tableNumber = tableFromNote,
                        note = heldTrx.transaction.note
                    ),
                    isHeldOrdersListDialogOpen = false,
                    isHoldDialogOpen = false,
                    isTableFloorDialogOpen = false
                )
            }

            transactionRepository.deleteHeldTransaction(heldTrx.transaction.id)
        }
    }

    fun deleteHeldOrder(heldTrxId: String) {
        viewModelScope.launch {
            transactionRepository.deleteHeldTransaction(heldTrxId)
        }
    }

    fun openTableFloorDialog(isPickerMode: Boolean = false) {
        _uiState.update { it.copy(isTableFloorDialogOpen = true, isTablePickerMode = isPickerMode) }
    }

    fun closeTableFloorDialog() {
        _uiState.update { it.copy(isTableFloorDialogOpen = false, isTablePickerMode = false) }
    }

    fun setTableNumber(tableNumber: String?) {
        _uiState.update {
            it.copy(
                cart = it.cart.copy(
                    tableNumber = tableNumber,
                    orderType = if (tableNumber != null) OrderType.DINE_IN else it.cart.orderType
                )
            )
        }
    }

    fun saveTable(table: RestaurantTableEntity) {
        viewModelScope.launch {
            tableRepository.saveTable(table)
        }
    }

    fun deleteTable(table: RestaurantTableEntity) {
        viewModelScope.launch {
            tableRepository.deleteTable(table)
        }
    }

    fun toggleTableOccupied(tableNumber: String, isOccupied: Boolean) {
        viewModelScope.launch {
            tableRepository.updateOccupiedStatus(tableNumber, isOccupied)
        }
    }

    fun seedDefaultTables() {
        viewModelScope.launch {
            tableRepository.seedDefaultTablesIfEmpty()
        }
    }
}

