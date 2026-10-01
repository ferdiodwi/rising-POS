package com.rising.pos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "modifiers")
data class ModifierEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Long = 0L,
    @ColumnInfo(name = "is_required")
    val isRequired: Boolean = false,
    @ColumnInfo(name = "is_multiple_select")
    val isMultipleSelect: Boolean = true,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

@Entity(
    tableName = "product_modifier_cross_ref",
    primaryKeys = ["product_id", "modifier_id"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ModifierEntity::class,
            parentColumns = ["id"],
            childColumns = ["modifier_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["modifier_id"])
    ]
)
data class ProductModifierCrossRef(
    @ColumnInfo(name = "product_id")
    val productId: String,
    @ColumnInfo(name = "modifier_id")
    val modifierId: String
)
