package com.rising.pos.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.ProductRepository
import com.rising.pos.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StockActionType {
    RESTOCK,      // Stok Masuk
    ADJUSTMENT,   // Koreksi Fisik
    DAMAGE_LOSS   // Rusak / Hilang
}

data class StockFormState(
    val actionType: StockActionType = StockActionType.RESTOCK,
    val selectedProduct: ProductEntity? = null,
    val amountInput: String = "",
    val reasonInput: String = "",
    val isLoss: Boolean = false
)

data class InventoryUiState(
    val selectedTab: Int = 0, // 0 = Daftar Stok Produk, 1 = Riwayat Mutasi
    val isFormOpen: Boolean = false,
    val formState: StockFormState = StockFormState()
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val trackedProducts: StateFlow<List<ProductEntity>> = productRepository.getAllProducts().map { list ->
        list.map { it.product }.filter { it.trackStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentMovements: StateFlow<List<StockMovementEntity>> = stockRepository.getRecentMovements(limit = 100).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    fun selectTab(tabIndex: Int) = _uiState.update { it.copy(selectedTab = tabIndex) }

    fun openStockAction(actionType: StockActionType, product: ProductEntity? = null) {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                formState = StockFormState(
                    actionType = actionType,
                    selectedProduct = product
                )
            )
        }
    }

    fun closeForm() = _uiState.update { it.copy(isFormOpen = false) }

    fun updateSelectedProduct(product: ProductEntity) {
        _uiState.update { it.copy(formState = it.formState.copy(selectedProduct = product)) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(formState = it.formState.copy(amountInput = amount.filter { ch -> ch.isDigit() })) }
    }

    fun updateReason(reason: String) {
        _uiState.update { it.copy(formState = it.formState.copy(reasonInput = reason)) }
    }

    fun submitStockAction() {
        val form = _uiState.value.formState
        val product = form.selectedProduct ?: return
        val amount = form.amountInput.toDoubleOrNull() ?: return

        viewModelScope.launch {
            when (form.actionType) {
                StockActionType.RESTOCK -> {
                    stockRepository.recordRestock(product.id, amount, form.reasonInput.ifBlank { null })
                }
                StockActionType.ADJUSTMENT -> {
                    stockRepository.recordAdjustment(product.id, amount, form.reasonInput.ifBlank { null })
                }
                StockActionType.DAMAGE_LOSS -> {
                    stockRepository.recordDamageOrLoss(product.id, amount, form.isLoss, form.reasonInput.ifBlank { null })
                }
            }
            _uiState.update { it.copy(isFormOpen = false) }
        }
    }
}
