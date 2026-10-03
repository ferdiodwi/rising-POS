package com.rising.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.ProductWithVariants
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 AND category_id = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE is_active = 1 AND is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteProducts(): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("""
        SELECT * FROM products 
        WHERE is_active = 1 
        AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE (barcode = :barcode OR sku = :barcode) AND is_active = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductWithCategory?

    @Query("SELECT * FROM product_variants WHERE (barcode = :barcode OR sku = :barcode) LIMIT 1")
    suspend fun getVariantByBarcode(barcode: String): ProductVariantEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductWithDetails(id: String): Flow<ProductWithVariants?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock, updated_at = :updatedAt WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Double, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE track_stock = 1 AND stock <= min_stock AND is_active = 1 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    fun getProductCount(): Flow<Int>

    // Variants
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Query("DELETE FROM product_variants WHERE product_id = :productId")
    suspend fun deleteVariantsByProductId(productId: String)

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductWithVariants(productId: String): ProductWithVariants?

    @Query("SELECT * FROM product_variants WHERE product_id = :productId")
    fun getVariantsByProductId(productId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE product_id = :productId")
    suspend fun getVariantsByProductIdSync(productId: String): List<ProductVariantEntity>
}
