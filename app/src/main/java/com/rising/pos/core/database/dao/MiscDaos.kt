package com.rising.pos.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rising.pos.core.database.entity.RestaurantTableEntity
import com.rising.pos.core.database.entity.SyncQueueEntity
import com.rising.pos.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantTableDao {
    @Query("SELECT * FROM restaurant_tables ORDER BY table_number ASC")
    fun getAllTables(): Flow<List<RestaurantTableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTableEntity)

    @Update
    suspend fun updateTable(table: RestaurantTableEntity)

    @Delete
    suspend fun deleteTable(table: RestaurantTableEntity)

    @Query("SELECT * FROM restaurant_tables WHERE id = :id LIMIT 1")
    suspend fun getTableById(id: String): RestaurantTableEntity?

    @Query("SELECT * FROM restaurant_tables WHERE table_number = :tableNumber LIMIT 1")
    suspend fun getTableByNumber(tableNumber: String): RestaurantTableEntity?

    @Query("UPDATE restaurant_tables SET is_occupied = :isOccupied, current_transaction_id = :transactionId WHERE table_number = :tableNumber")
    suspend fun updateOccupiedStatusByNumber(tableNumber: String, isOccupied: Boolean, transactionId: String?)

    @Query("UPDATE restaurant_tables SET is_occupied = :isOccupied, current_transaction_id = :transactionId WHERE id = :id")
    suspend fun updateOccupiedStatusById(id: String, isOccupied: Boolean, transactionId: String?)

    @Query("UPDATE restaurant_tables SET is_occupied = 0, current_transaction_id = NULL WHERE current_transaction_id = :transactionId")
    suspend fun releaseTableByTransactionId(transactionId: String)

    @Query("SELECT COUNT(*) FROM restaurant_tables")
    suspend fun countTables(): Int
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPendingQueue(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue ORDER BY created_at DESC")
    fun getAllQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, retry_count = retry_count + 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: SyncStatus)

    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE status = 'PENDING' OR status = 'SYNCING'")
    suspend fun markAllSynced()

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearSynced()
}
