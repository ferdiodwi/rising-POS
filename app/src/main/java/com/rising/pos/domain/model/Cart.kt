package com.rising.pos.domain.model

import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.model.OrderType
import java.util.UUID

data class CartItem(
    val cartItemId: String = UUID.randomUUID().toString(),
    val product: ProductEntity,
    val variant: ProductVariantEntity? = null,
    val selectedModifiers: List<ModifierEntity> = emptyList(),
    val quantity: Double = 1.0,
    val discount: Double = 0.0,
    val note: String? = null
) {
    val unitPrice: Double
        get() = product.sellingPrice +
                (variant?.priceAdjustment ?: 0.0) +
                selectedModifiers.sumOf { it.price }

    val totalPrice: Double
        get() = maxOf(0.0, (unitPrice * quantity) - discount)
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val discount: Double = 0.0,
    val discountReason: String? = null,
    val orderType: OrderType = OrderType.RETAIL,
    val customer: CustomerEntity? = null,
    val tableNumber: String? = null,
    val note: String? = null
) {
    val totalItemCount: Double
        get() = items.sumOf { it.quantity }

    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    fun calculateTotals(
        isTaxEnabled: Boolean,
        taxPercentage: Double,
        isTaxInclusive: Boolean,
        isServiceChargeEnabled: Boolean,
        serviceChargePercentage: Double
    ): CartCalculation {
        val netSubtotal = maxOf(0.0, subtotal - discount)

        val serviceCharge = if (isServiceChargeEnabled && serviceChargePercentage > 0) {
            netSubtotal * (serviceChargePercentage / 100.0)
        } else 0.0

        val taxableAmount = netSubtotal + serviceCharge

        val tax = if (isTaxEnabled && taxPercentage > 0) {
            if (isTaxInclusive) {
                // Inclusive: tax is already included in the prices
                taxableAmount - (taxableAmount / (1 + (taxPercentage / 100.0)))
            } else {
                // Exclusive: tax is added on top
                taxableAmount * (taxPercentage / 100.0)
            }
        } else 0.0

        val grandTotal = if (isTaxEnabled && !isTaxInclusive) {
            netSubtotal + serviceCharge + tax
        } else {
            netSubtotal + serviceCharge
        }

        return CartCalculation(
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            serviceCharge = serviceCharge,
            grandTotal = grandTotal
        )
    }
}

data class CartCalculation(
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val serviceCharge: Double,
    val grandTotal: Double
)
