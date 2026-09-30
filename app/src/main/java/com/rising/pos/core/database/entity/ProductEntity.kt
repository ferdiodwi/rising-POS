package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rising.pos.core.model.SyncStatus

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["name"]),
        Index(value = ["category_id"]),
        Index(value = ["barcode"]),
        Index(value = ["sku"]),
        Index(value = ["is_favorite"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val sku: String? = null,
    val barcode: String? = null,
    val name: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double,
    @ColumnInfo(name = "cost_price")
    val costPrice: Double = 0.0,
    val stock: Double = 0.0,
    val unit: String = "pcs",
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "track_stock")
    val trackStock: Boolean = true,
    @ColumnInfo(name = "min_stock")
    val minStock: Double = 5.0,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
