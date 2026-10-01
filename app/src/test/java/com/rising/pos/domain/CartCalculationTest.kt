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
        sellingPrice = 15000L,
        stock = 10.0
    )

    private val sampleProduct2 = ProductEntity(
        id = "p2",
        name = "Croissant",
        sellingPrice = 25000L,
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

        assertEquals(55000L, calc.subtotal)
        assertEquals(0L, calc.discount)
        assertEquals(0L, calc.tax)
        assertEquals(55000L, calc.grandTotal)
    }

    @Test
    fun `test calculation with cart discount`() {
        val cart = CartState(
            items = listOf(
                CartItem(product = sampleProduct1, quantity = 2.0) // 30,000
            ),
            discount = 5000L
        )

        val calc = cart.calculateTotals(
            isTaxEnabled = false,
            taxPercentage = 0.0,
            isTaxInclusive = false,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertEquals(30000L, calc.subtotal)
        assertEquals(5000L, calc.discount)
        assertEquals(25000L, calc.grandTotal)
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

        assertEquals(3000L, calc.tax)
        assertEquals(33000L, calc.grandTotal)
    }

    @Test
    fun `test inclusive tax calculation`() {
        // Harga 33,000 sudah termasuk pajak 10% -> pajak = 3,000
        val product = ProductEntity(id = "p3", name = "Teh", sellingPrice = 33000L, stock = 5.0)
        val cart = CartState(items = listOf(CartItem(product = product, quantity = 1.0)))

        val calc = cart.calculateTotals(
            isTaxEnabled = true,
            taxPercentage = 10.0,
            isTaxInclusive = true,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertEquals(3000L, calc.tax)
        assertEquals("grandTotal tidak berubah pada pajak inklusif", 33000L, calc.grandTotal)
    }

    @Test
    fun `test pecahan kuantitas dibulatkan ke rupiah`() {
        // 0.5 kg x 15,000 = 7,500 (pecahan kuantitas, harga bulat)
        val cart = CartState(
            items = listOf(CartItem(product = sampleProduct1, quantity = 0.5))
        )
        val calc = cart.calculateTotals()
        assertEquals(7500L, calc.grandTotal)
    }

    @Test
    fun `test kuantitas menghasilkan pecahan rupiah dibulatkan`() {
        // 3 x 3,333 = 9,999 (tanpa pecahan). Pakai harga yang menghasilkan pecahan:
        // 1.5 x 3,333 = 4,999.5 -> dibulatkan 5,000
        val product = ProductEntity(id = "p4", name = "Permen", sellingPrice = 3333L, stock = 10.0)
        val cart = CartState(items = listOf(CartItem(product = product, quantity = 1.5)))
        val calc = cart.calculateTotals()
        assertEquals(5000L, calc.grandTotal)
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
