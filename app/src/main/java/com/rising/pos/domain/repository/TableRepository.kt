package com.rising.pos.domain.repository

import com.rising.pos.core.database.entity.RestaurantTableEntity
import kotlinx.coroutines.flow.Flow

interface TableRepository {
    fun getAllTables(): Flow<List<RestaurantTableEntity>>
    suspend fun getTableById(id: String): RestaurantTableEntity?
    suspend fun getTableByNumber(tableNumber: String): RestaurantTableEntity?
    suspend fun saveTable(table: RestaurantTableEntity): Result<Unit>
    suspend fun deleteTable(table: RestaurantTableEntity): Result<Unit>
    suspend fun updateOccupiedStatus(tableNumber: String, isOccupied: Boolean, transactionId: String? = null): Result<Unit>
    suspend fun releaseTableByTransactionId(transactionId: String): Result<Unit>
    suspend fun seedDefaultTablesIfEmpty(): Result<Unit>
}
