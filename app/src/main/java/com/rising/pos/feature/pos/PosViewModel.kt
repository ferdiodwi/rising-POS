package com.rising.pos.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.domain.repository.ProductRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PosUiState(
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val cart: CartState = CartState(),
    val isCheckoutDialogOpen: Boolean = false,
    val isCartSheetOpen: Boolean = false,
    val isHoldDialogOpen: Boolean = false,
    val isHeldOrdersListDialogOpen: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val paymentErrorMessage: String? = null,
    val lastCompletedTransaction: TransactionWithDetails? = null,
    val isPrinting: Boolean = false,
    val printMessage: String? = null,
    val printErrorMessage: String? = null
)

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val appPreferences: AppPreferences,
    private val printerManager: BluetoothPrinterManager
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
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

    fun scanBarcode(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val matched = productRepository.getProductByBarcode(trimmed)
            if (matched != null) {
                addToCart(matched.product)
                _uiState.update { it.copy(searchQuery = "") }
            } else {
                updateSearchQuery(trimmed)
            }
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
        cashPaidAmount: Double,
        orderType: OrderType = _uiState.value.cart.orderType,
        note: String? = _uiState.value.cart.note
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

        val paidAmount = if (paymentMethod == PaymentMethod.CASH) cashPaidAmount else calc.grandTotal

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
                serviceChargePercentage = currentSettings.serviceChargePercentage
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
                    costPrice = 0.0,
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

            _uiState.update {
                it.copy(
                    cart = CartState(
                        items = restoredCartItems,
                        discount = heldTrx.transaction.discount,
                        discountReason = heldTrx.transaction.discountReason,
                        orderType = heldTrx.transaction.orderType,
                        note = heldTrx.transaction.note
                    ),
                    isHeldOrdersListDialogOpen = false,
                    isHoldDialogOpen = false
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
}

