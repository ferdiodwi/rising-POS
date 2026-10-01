package com.rising.pos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rising.pos.core.database.dao.CategoryDao
import com.rising.pos.core.database.dao.CustomerDao
import com.rising.pos.core.database.dao.ExpenseDao
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.dao.RestaurantTableDao
import com.rising.pos.core.database.dao.StockMovementDao
import com.rising.pos.core.database.dao.SyncQueueDao
import com.rising.pos.core.database.dao.TransactionDao
import com.rising.pos.core.database.entity.CategoryEntity
import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.ExpenseEntity
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductModifierCrossRef
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.database.entity.SyncQueueEntity
import com.rising.pos.core.database.entity.TransactionEntity
import com.rising.pos.core.database.entity.TransactionItemEntity
import com.rising.pos.core.database.entity.TransactionItemModifierEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        ModifierEntity::class,
        ProductModifierCrossRef::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        TransactionItemModifierEntity::class,
        StockMovementEntity::class,
        CustomerEntity::class,
        ExpenseEntity::class,
        RestaurantTableEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun customerDao(): CustomerDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun restaurantTableDao(): RestaurantTableDao
    abstract fun syncQueueDao(): SyncQueueDao
}
