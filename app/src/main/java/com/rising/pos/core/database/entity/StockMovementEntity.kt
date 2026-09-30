package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.core.model.SyncStatus

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["created_at"]),
        Index(value = ["reference_id"]),
        Index(value = ["type"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "product_id")
    val productId: String,
    val type: StockMovementType,
    @ColumnInfo(name = "qty_change")
    val qtyChange: Double,
    @ColumnInfo(name = "qty_before")
    val qtyBefore: Double,
    @ColumnInfo(name = "qty_after")
    val qtyAfter: Double,
    val reason: String? = null,
    @ColumnInfo(name = "reference_id")
    val referenceId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
