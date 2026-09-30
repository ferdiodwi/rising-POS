package com.rising.pos.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.rising.pos.core.database.entity.ProductVariantEntity

data class VariantFormItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: String = "",
    val stock: String = "0"
)

data class ProductFormState(
    val id: String = "",
    val name: String = "",
    val categoryId: String? = null,
    val sellingPrice: String = "",
    val costPrice: String = "",
    val stock: String = "0",
    val unit: String = "pcs",
    val barcode: String = "",
    val sku: String = "",
    val trackStock: Boolean = true,
    val minStock: String = "5",
    val isFavorite: Boolean = false,
    val hasVariants: Boolean = false,
    val variants: List<VariantFormItem> = emptyList()
)

data class ProductUiState(
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isFormOpen: Boolean = false,
    val formState: ProductFormState = ProductFormState(),
    val isCategoryDialogOpen: Boolean = false,
    val newCategoryName: String = ""
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val settings: StateFlow<BusinessSettings> = appPreferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettings()
    )

    val categories: StateFlow<List<CategoryEntity>> = productRepository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState = _uiState.asStateFlow()

    val filteredProducts: StateFlow<List<ProductWithCategory>> = combine(
        productRepository.getAllProducts(),
        _uiState
    ) { allProducts, state ->
        allProducts.filter { item ->
            val matchCat = state.selectedCategoryId == null || item.product.categoryId == state.selectedCategoryId
            val matchSearch = state.searchQuery.isBlank() ||
                    item.product.name.contains(state.searchQuery, ignoreCase = true) ||
                    (item.product.barcode != null && item.product.barcode.contains(state.searchQuery, ignoreCase = true))
            matchCat && matchSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun selectCategory(catId: String?) = _uiState.update { it.copy(selectedCategoryId = catId) }

    fun openAddProductForm() {
        _uiState.update {
            it.copy(
                isFormOpen = true,
                formState = ProductFormState(id = UUID.randomUUID().toString())
            )
        }
    }

    fun openEditProductForm(item: ProductWithCategory) {
        val p = item.product
        viewModelScope.launch {
            val variants = productRepository.getVariantsByProductId(p.id)
            val variantItems = variants.map {
                val finalPrice = p.sellingPrice + it.priceAdjustment
                VariantFormItem(
                    id = it.id,
                    name = it.name,
                    price = finalPrice.toInt().toString(),
                    stock = (it.stock ?: p.stock).toInt().toString()
                )
            }
            _uiState.update {
                it.copy(
                    isFormOpen = true,
                    formState = ProductFormState(
                        id = p.id,
                        name = p.name,
                        categoryId = p.categoryId,
                        sellingPrice = p.sellingPrice.toInt().toString(),
                        costPrice = p.costPrice.toInt().toString(),
                        stock = p.stock.toInt().toString(),
                        unit = p.unit,
                        barcode = p.barcode ?: "",
                        sku = p.sku ?: "",
                        trackStock = p.trackStock,
                        minStock = p.minStock.toInt().toString(),
                        isFavorite = p.isFavorite,
                        hasVariants = variantItems.isNotEmpty(),
                        variants = variantItems
                    )
                )
            }
        }
    }

    fun closeForm() {
        _uiState.update { it.copy(isFormOpen = false) }
    }

    fun updateForm(form: ProductFormState) {
        _uiState.update { it.copy(formState = form) }
    }

    fun addVariant() {
        val current = _uiState.value.formState
        val defaultPrice = current.sellingPrice
        val newVariant = VariantFormItem(price = defaultPrice)
        _uiState.update {
            it.copy(
                formState = current.copy(
                    hasVariants = true,
                    variants = current.variants + newVariant
                )
            )
        }
    }

    fun removeVariant(variantId: String) {
        val current = _uiState.value.formState
        val updated = current.variants.filter { it.id != variantId }
        _uiState.update {
            it.copy(
                formState = current.copy(
                    variants = updated,
                    hasVariants = updated.isNotEmpty()
                )
            )
        }
    }

    fun updateVariant(variantId: String, name: String, price: String, stock: String) {
        val current = _uiState.value.formState
        val updated = current.variants.map {
            if (it.id == variantId) it.copy(name = name, price = price, stock = stock)
            else it
        }
        _uiState.update {
            it.copy(formState = current.copy(variants = updated))
        }
    }

    fun saveProduct() {
        val form = _uiState.value.formState
        if (form.name.isBlank()) return

        val sellingPrice = form.sellingPrice.toDoubleOrNull() ?: 0.0
        val costPrice = form.costPrice.toDoubleOrNull() ?: 0.0
        val stock = form.stock.toDoubleOrNull() ?: 0.0
        val minStock = form.minStock.toDoubleOrNull() ?: 5.0

        val productEntity = ProductEntity(
            id = form.id.ifBlank { UUID.randomUUID().toString() },
            name = form.name.trim(),
            categoryId = form.categoryId,
            sellingPrice = sellingPrice,
            costPrice = costPrice,
            stock = stock,
            unit = form.unit.trim().ifBlank { "pcs" },
            barcode = form.barcode.trim().ifBlank { null },
            sku = form.sku.trim().ifBlank { null },
            trackStock = form.trackStock,
            minStock = minStock,
            isFavorite = form.isFavorite,
            isActive = true
        )

        val variantEntities = if (form.hasVariants) {
            form.variants.filter { it.name.isNotBlank() }.map { v ->
                val variantPrice = v.price.toDoubleOrNull() ?: sellingPrice
                ProductVariantEntity(
                    id = v.id,
                    productId = productEntity.id,
                    name = v.name.trim(),
                    priceAdjustment = variantPrice - sellingPrice,
                    stock = v.stock.toDoubleOrNull()
                )
            }
        } else emptyList()

        viewModelScope.launch {
            productRepository.saveProduct(productEntity, variantEntities)
            _uiState.update { it.copy(isFormOpen = false) }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun openCategoryDialog() {
        _uiState.update { it.copy(isCategoryDialogOpen = true, newCategoryName = "") }
    }

    fun closeCategoryDialog() {
        _uiState.update { it.copy(isCategoryDialogOpen = false) }
    }

    fun updateNewCategoryName(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun saveCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            productRepository.saveCategory(
                CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sortOrder = categories.value.size,
                    isActive = true
                )
            )
            _uiState.update { it.copy(isCategoryDialogOpen = false) }
        }
    }
}
