package com.rising.pos.domain.model

import com.rising.pos.core.database.entity.CustomerEntity
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductVariantEntity
import com.rising.pos.core.model.OrderType
import java.util.UUID
import kotlin.math.roundToLong

/**
 * CATATAN TIPE UANG: nominal uang = `Long` (rupiah bulat); kuantitas = `Double`.
 * Perkalian kuantitas (pecahan) × harga menghasilkan pecahan rupiah, jadi tiap
 * hasil dibulatkan ke rupiah terdekat via [roundToLong] untuk menjaga akurasi.
 */
data class CartItem(
    val cartItemId: String = UUID.randomUUID().toString(),
    val product: ProductEntity,
    val variant: ProductVariantEntity? = null,
    val selectedModifiers: List<ModifierEntity> = emptyList(),
    val quantity: Double = 1.0,
    val discount: Long = 0L,
    val note: String? = null
) {
    val unitPrice: Long
        get() = product.sellingPrice +
                (variant?.priceAdjustment ?: 0L) +
                selectedModifiers.sumOf { it.price }

    val totalPrice: Long
        get() = ((unitPrice * quantity).roundToLong() - discount).coerceAtLeast(0L)
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val discount: Long = 0L,
    val discountReason: String? = null,
    val orderType: OrderType = OrderType.RETAIL,
    val customer: CustomerEntity? = null,
    val tableNumber: String? = null,
    val note: String? = null
) {
    val totalItemCount: Double
        get() = items.sumOf { it.quantity }

    val subtotal: Long
        get() = items.sumOf { it.totalPrice }

    fun calculateTotals(
        isTaxEnabled: Boolean = false,
        taxPercentage: Double = 0.0,
        isTaxInclusive: Boolean = true,
        isServiceChargeEnabled: Boolean = false,
        serviceChargePercentage: Double = 0.0
    ): CartCalculation {

        val netSubtotal = (subtotal - discount).coerceAtLeast(0L)

        val serviceCharge = if (isServiceChargeEnabled && serviceChargePercentage > 0) {
            (netSubtotal * (serviceChargePercentage / 100.0)).roundToLong()
        } else 0L

        val taxableAmount = netSubtotal + serviceCharge

        val tax = if (isTaxEnabled && taxPercentage > 0) {
            if (isTaxInclusive) {
                // Inclusive: pajak sudah termasuk dalam harga.
                (taxableAmount - (taxableAmount / (1 + (taxPercentage / 100.0)))).roundToLong()
            } else {
                // Exclusive: pajak ditambahkan di atas harga.
                (taxableAmount * (taxPercentage / 100.0)).roundToLong()
            }
        } else 0L

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
    val subtotal: Long,
    val discount: Long,
    val tax: Long,
    val serviceCharge: Long,
    val grandTotal: Long
)
