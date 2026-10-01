package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rising.pos.core.model.SyncStatus

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val amount: Long,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    @ColumnInfo(name = "receipt_image_url")
    val receiptImageUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
