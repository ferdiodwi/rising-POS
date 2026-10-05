package com.rising.pos.data.repository

import com.rising.pos.core.database.dao.CategoryDao
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.ProductWithVariants
import com.rising.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductModifierCrossRef

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<ProductWithCategory>> = productDao.getAllProducts()

    override fun getActiveProducts(): Flow<List<ProductWithCategory>> = productDao.getActiveProducts()

    override fun getProductsByCategory(categoryId: String): Flow<List<ProductWithCategory>> =
        productDao.getProductsByCategory(categoryId)

    override fun getFavoriteProducts(): Flow<List<ProductWithCategory>> = productDao.getFavoriteProducts()

    override fun searchProducts(query: String): Flow<List<ProductWithCategory>> =
        productDao.searchProducts(query)

    override suspend fun getProductByBarcode(barcode: String): ProductWithCategory? =
        productDao.getProductByBarcode(barcode)

    override suspend fun getVariantByBarcode(barcode: String): ProductVariantEntity? =
        productDao.getVariantByBarcode(barcode)

    override suspend fun getProductById(id: String): ProductEntity? =
        productDao.getProductById(id)

    override fun getProductDetails(id: String): Flow<ProductWithVariants?> =
        productDao.getProductWithDetails(id)

    override suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity> =
        productDao.getVariantsByProductIdSync(productId)

    suspend fun saveProduct(product: ProductEntity, variants: List<ProductVariantEntity>) {
        saveProduct(product, variants, emptyList())
    }

    override suspend fun saveProduct(
        product: ProductEntity,
        variants: List<ProductVariantEntity>,
        modifierIds: List<String>
    ) {
        val existing = productDao.getProductById(product.id)
        if (existing != null) {
            // PENTING: gunakan UPDATE, bukan INSERT OR REPLACE.
            // INSERT OR REPLACE di SQLite = DELETE baris lama + INSERT baru. Karena
            // tabel stock_movements punya ForeignKey(onDelete = CASCADE) ke products,
            // setiap kali produk di-edit lewat jalur REPLACE, SELURUH riwayat mutasi
            // stok produk tersebut ikut terhapus. UPDATE tidak menghapus baris, jadi
            // riwayat mutasi tetap utuh. createdAt lama dipertahankan.
            productDao.updateProduct(product.copy(createdAt = existing.createdAt))
        } else {
            productDao.insertProduct(product)
        }
        if (variants.isNotEmpty()) {
            productDao.deleteVariantsByProductId(product.id)
            productDao.insertVariants(variants)
        }
        
        // Sync product modifier cross references
        productDao.deleteProductModifierCrossRefs(product.id)
        if (modifierIds.isNotEmpty()) {
            val crossRefs = modifierIds.map { modId ->
                ProductModifierCrossRef(
                    productId = product.id,
                    modifierId = modId
                )
            }
            productDao.insertProductModifierCrossRefs(crossRefs)
        }
    }

    override suspend fun updateStock(productId: String, newStock: Double) {
        productDao.updateStock(productId, newStock)
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    override fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    override fun getProductCount(): Flow<Int> = productDao.getProductCount()

    override fun getAllModifiers(): Flow<List<ModifierEntity>> = productDao.getAllModifiers()

    override suspend fun getModifiersByProductId(productId: String): List<ModifierEntity> =
        productDao.getModifiersByProductIdSync(productId)

    override suspend fun saveModifier(modifier: ModifierEntity) {
        val existing = productDao.getModifierById(modifier.id)
        if (existing != null) {
            productDao.updateModifier(modifier)
        } else {
            productDao.insertModifier(modifier)
        }
    }

    override suspend fun deleteModifier(modifier: ModifierEntity) {
        productDao.deleteModifier(modifier)
    }

    override suspend fun seedDefaultModifiersIfEmpty() {
        if (productDao.countModifiers() == 0) {
            val defaultModifiers = listOf(
                ModifierEntity(
                    id = "mod-extra-shot",
                    name = "Extra Espresso Shot",
                    price = 5000,
                    isRequired = false,
                    isMultipleSelect = true,
                    isActive = true
                ),
                ModifierEntity(
                    id = "mod-less-sugar",
                    name = "Less Sugar",
                    price = 0,
                    isRequired = false,
                    isMultipleSelect = false,
                    isActive = true
                ),
                ModifierEntity(
                    id = "mod-extra-sugar",
                    name = "Extra Sugar",
                    price = 0,
                    isRequired = false,
                    isMultipleSelect = false,
                    isActive = true
                ),
                ModifierEntity(
                    id = "mod-boba",
                    name = "Topping Boba",
                    price = 4000,
                    isRequired = false,
                    isMultipleSelect = true,
                    isActive = true
                ),
                ModifierEntity(
                    id = "mod-cheese-slice",
                    name = "Keju Slice / Cheese",
                    price = 3000,
                    isRequired = false,
                    isMultipleSelect = true,
                    isActive = true
                ),
                ModifierEntity(
                    id = "mod-sambal-extra",
                    name = "Sambal Ekstra",
                    price = 2000,
                    isRequired = false,
                    isMultipleSelect = true,
                    isActive = true
                )
            )
            productDao.insertModifiers(defaultModifiers)
        }
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    override fun getActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.getActiveCategories()

    override suspend fun saveCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
}
