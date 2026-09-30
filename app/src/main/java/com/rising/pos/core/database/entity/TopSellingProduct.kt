package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo

data class TopSellingProduct(
    @ColumnInfo(name = "product_id")
    val productId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "total_qty")
    val totalQty: Double,
    @ColumnInfo(name = "total_revenue")
    val totalRevenue: Double
)
