package com.rising.pos.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ProductWithCategory(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

data class ProductWithVariants(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val variants: List<ProductVariantEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductModifierCrossRef::class,
            parentColumn = "product_id",
            entityColumn = "modifier_id"
        )
    )
    val modifiers: List<ModifierEntity>
)

data class TransactionItemWithModifiers(
    @Embedded val item: TransactionItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transaction_item_id"
    )
    val modifiers: List<TransactionItemModifierEntity>
)

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        entity = TransactionItemEntity::class,
        parentColumn = "id",
        entityColumn = "transaction_id"
    )
    val items: List<TransactionItemWithModifiers>
)
