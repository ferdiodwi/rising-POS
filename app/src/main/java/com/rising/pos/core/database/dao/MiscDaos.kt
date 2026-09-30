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
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPendingQueue(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, retry_count = retry_count + 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: SyncStatus)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)
}
