package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"])
    ]
)
data class ProductVariantEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "product_id")
    val productId: String,
    val name: String,
    @ColumnInfo(name = "price_adjustment")
    val priceAdjustment: Double = 0.0,
    val sku: String? = null,
    val barcode: String? = null,
    val stock: Double? = null
)
