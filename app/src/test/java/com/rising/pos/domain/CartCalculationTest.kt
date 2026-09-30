package com.rising.pos.domain

import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.util.ReceiptNumberGenerator
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartCalculationTest {

    private val sampleProduct1 = ProductEntity(
        id = "p1",
        name = "Kopi Susu",
        sellingPrice = 15000.0,
        stock = 10.0
    )

    private val sampleProduct2 = ProductEntity(
        id = "p2",
        name = "Croissant",
        sellingPrice = 25000.0,
        stock = 5.0
    )

    @Test
    fun `test subtotal calculation without tax or discount`() {
        val cart = CartState(
            items = listOf(
                CartItem(product = sampleProduct1, quantity = 2.0), // 30,000
                CartItem(product = sampleProduct2, quantity = 1.0)  // 25,000
            )
        )

        val calc = cart.calculateTotals(
            isTaxEnabled = false,
            taxPercentage = 0.0,
            isTaxInclusive = false,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertEquals(55000.0, calc.subtotal, 0.01)
        assertEquals(0.0, calc.discount, 0.01)
        assertEquals(0.0, calc.tax, 0.01)
        assertEquals(55000.0, calc.grandTotal, 0.01)
    }

    @Test
    fun `test calculation with cart discount`() {
        val cart = CartState(
            items = listOf(
                CartItem(product = sampleProduct1, quantity = 2.0) // 30,000
            ),
            discount = 5000.0
        )

        val calc = cart.calculateTotals(
            isTaxEnabled = false,
            taxPercentage = 0.0,
            isTaxInclusive = false,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertEquals(30000.0, calc.subtotal, 0.01)
        assertEquals(5000.0, calc.discount, 0.01)
        assertEquals(25000.0, calc.grandTotal, 0.01)
    }

    @Test
    fun `test exclusive tax calculation`() {
        val cart = CartState(
            items = listOf(
                CartItem(product = sampleProduct1, quantity = 2.0) // 30,000
            )
        )

        // 10% exclusive tax on 30,000 -> tax = 3,000, total = 33,000
        val calc = cart.calculateTotals(
            isTaxEnabled = true,
            taxPercentage = 10.0,
            isTaxInclusive = false,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertEquals(3000.0, calc.tax, 0.01)
        assertEquals(33000.0, calc.grandTotal, 0.01)
    }

    @Test
    fun `test offline receipt number generation format`() {
        val receiptNumber = ReceiptNumberGenerator.generate(
            deviceId = "A01",
            sequenceNumber = 42,
            timestamp = 1759230000000L
        )

        assertTrue(receiptNumber.startsWith("TRX-A01-"))
        assertTrue(receiptNumber.endsWith("-00042"))
    }
}
