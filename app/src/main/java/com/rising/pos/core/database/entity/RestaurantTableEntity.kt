package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rising.pos.core.model.SyncStatus

@Entity(
    tableName = "restaurant_tables",
    indices = [
        Index(value = ["table_number"], unique = true)
    ]
)
data class RestaurantTableEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "table_number")
    val tableNumber: String,
    val capacity: Int = 4,
    @ColumnInfo(name = "is_occupied")
    val isOccupied: Boolean = false,
    @ColumnInfo(name = "current_transaction_id")
    val currentTransactionId: String? = null
)

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    val action: String, // INSERT, UPDATE, DELETE
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    val status: SyncStatus = SyncStatus.PENDING,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
