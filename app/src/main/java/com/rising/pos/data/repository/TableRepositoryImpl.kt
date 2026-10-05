package com.rising.pos.data.repository

import com.rising.pos.core.database.dao.RestaurantTableDao
import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.domain.repository.TableRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepositoryImpl @Inject constructor(
    private val restaurantTableDao: RestaurantTableDao
) : TableRepository {

    override fun getAllTables(): Flow<List<RestaurantTableEntity>> {
        return restaurantTableDao.getAllTables()
    }

    override suspend fun getTableById(id: String): RestaurantTableEntity? {
        return restaurantTableDao.getTableById(id)
    }

    override suspend fun getTableByNumber(tableNumber: String): RestaurantTableEntity? {
        return restaurantTableDao.getTableByNumber(tableNumber.trim())
    }

    override suspend fun saveTable(table: RestaurantTableEntity): Result<Unit> = runCatching {
        val cleanNumber = table.tableNumber.trim()
        if (cleanNumber.isBlank()) {
            throw IllegalArgumentException("Nomor atau nama meja tidak boleh kosong")
        }
        val existing = restaurantTableDao.getTableByNumber(cleanNumber)
        if (existing != null && existing.id != table.id) {
            throw IllegalArgumentException("Meja dengan nama/nomor '$cleanNumber' sudah ada")
        }
        val cleanTable = table.copy(
            id = table.id.ifBlank { UUID.randomUUID().toString() },
            tableNumber = cleanNumber,
            capacity = if (table.capacity <= 0) 4 else table.capacity
        )
        restaurantTableDao.insertTable(cleanTable)
    }

    override suspend fun deleteTable(table: RestaurantTableEntity): Result<Unit> = runCatching {
        restaurantTableDao.deleteTable(table)
    }

    override suspend fun updateOccupiedStatus(
        tableNumber: String,
        isOccupied: Boolean,
        transactionId: String?
    ): Result<Unit> = runCatching {
        restaurantTableDao.updateOccupiedStatusByNumber(tableNumber.trim(), isOccupied, transactionId)
    }

    override suspend fun releaseTableByTransactionId(transactionId: String): Result<Unit> = runCatching {
        restaurantTableDao.releaseTableByTransactionId(transactionId)
    }

    override suspend fun seedDefaultTablesIfEmpty(): Result<Unit> = runCatching {
        val count = restaurantTableDao.countTables()
        if (count == 0) {
            val defaults = listOf(
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 01", capacity = 2),
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 02", capacity = 4),
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 03", capacity = 4),
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 04", capacity = 4),
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 05", capacity = 6),
                RestaurantTableEntity(id = UUID.randomUUID().toString(), tableNumber = "Meja 06", capacity = 8)
            )
            for (t in defaults) {
                restaurantTableDao.insertTable(t)
            }
        }
    }
}
