package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.ProductWithVariants
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<ProductWithCategory>>
    fun getActiveProducts(): Flow<List<ProductWithCategory>>
    fun getProductsByCategory(categoryId: String): Flow<List<ProductWithCategory>>
    fun getFavoriteProducts(): Flow<List<ProductWithCategory>>
    fun searchProducts(query: String): Flow<List<ProductWithCategory>>
    suspend fun getProductByBarcode(barcode: String): ProductWithCategory?
    suspend fun getProductById(id: String): ProductEntity?
    fun getProductDetails(id: String): Flow<ProductWithVariants?>
    suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity>
    suspend fun saveProduct(product: ProductEntity, variants: List<ProductVariantEntity> = emptyList())
    suspend fun updateStock(productId: String, newStock: Double)
    suspend fun deleteProduct(product: ProductEntity)
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    fun getProductCount(): Flow<Int>

    // Category
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getActiveCategories(): Flow<List<CategoryEntity>>
    suspend fun saveCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
}
