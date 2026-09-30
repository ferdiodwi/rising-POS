package com.rising.pos.core.database.di

import android.content.Context
import androidx.room.Room
import com.rising.pos.core.database.PosDatabase
import com.rising.pos.core.database.dao.CategoryDao
import com.rising.pos.core.database.dao.CustomerDao
import com.rising.pos.core.database.dao.ExpenseDao
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.dao.RestaurantTableDao
import com.rising.pos.core.database.dao.StockMovementDao
import com.rising.pos.core.database.dao.SyncQueueDao
import com.rising.pos.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePosDatabase(
        @ApplicationContext context: Context
    ): PosDatabase {
        return Room.databaseBuilder(
            context,
            PosDatabase::class.java,
            "rising_pos.db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    fun provideCategoryDao(database: PosDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideProductDao(database: PosDatabase): ProductDao = database.productDao()

    @Provides
    fun provideTransactionDao(database: PosDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideStockMovementDao(database: PosDatabase): StockMovementDao = database.stockMovementDao()

    @Provides
    fun provideCustomerDao(database: PosDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideExpenseDao(database: PosDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideRestaurantTableDao(database: PosDatabase): RestaurantTableDao = database.restaurantTableDao()

    @Provides
    fun provideSyncQueueDao(database: PosDatabase): SyncQueueDao = database.syncQueueDao()
}
