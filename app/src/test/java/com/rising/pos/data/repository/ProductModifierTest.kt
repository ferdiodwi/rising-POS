package com.rising.pos.data.repository

import com.google.common.truth.Truth.assertThat
import com.rising.pos.core.database.dao.CategoryDao
import com.rising.pos.core.database.dao.ProductDao
import com.rising.pos.core.database.entity.ModifierEntity
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.ProductModifierCrossRef
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProductModifierTest {

    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: ProductRepositoryImpl

    private val sampleProduct = ProductEntity(
        id = "prod-kopi",
        name = "Kopi Susu Gula Aren",
        sellingPrice = 18000,
        stock = 20.0
    )

    private val sampleModifier = ModifierEntity(
        id = "mod-extra-shot",
        name = "Extra Shot Espresso",
        price = 5000,
        isRequired = false,
        isMultipleSelect = true,
        isActive = true
    )

    @Before
    fun setUp() {
        productDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        repository = ProductRepositoryImpl(productDao, categoryDao)
    }

    @Test
    fun `getAllModifiers returns flow from dao`() = runTest {
        every { productDao.getAllModifiers() } returns flowOf(listOf(sampleModifier))

        val result = repository.getAllModifiers().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Extra Shot Espresso")
        assertThat(result[0].price).isEqualTo(5000L)
    }

    @Test
    fun `getModifiersByProductId calls dao sync`() = runTest {
        coEvery { productDao.getModifiersByProductIdSync("prod-kopi") } returns listOf(sampleModifier)

        val result = repository.getModifiersByProductId("prod-kopi")

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("mod-extra-shot")
        coVerify(exactly = 1) { productDao.getModifiersByProductIdSync("prod-kopi") }
    }

    @Test
    fun `saveProduct inserts cross refs when modifierIds provided`() = runTest {
        coEvery { productDao.getProductById("prod-kopi") } returns null

        repository.saveProduct(
            product = sampleProduct,
            variants = emptyList(),
            modifierIds = listOf("mod-extra-shot", "mod-boba")
        )

        coVerify(exactly = 1) { productDao.insertProduct(sampleProduct) }
        coVerify(exactly = 1) { productDao.deleteProductModifierCrossRefs("prod-kopi") }
        coVerify(exactly = 1) {
            productDao.insertProductModifierCrossRefs(match { list ->
                list.size == 2 &&
                        list.any { it.productId == "prod-kopi" && it.modifierId == "mod-extra-shot" } &&
                        list.any { it.productId == "prod-kopi" && it.modifierId == "mod-boba" }
            })
        }
    }

    @Test
    fun `saveProduct preserves existing createdAt when updating product`() = runTest {
        val existing = sampleProduct.copy(createdAt = 123456789L)
        coEvery { productDao.getProductById("prod-kopi") } returns existing

        val updated = sampleProduct.copy(name = "Kopi Susu Pandan", createdAt = 0L)
        repository.saveProduct(
            product = updated,
            variants = emptyList(),
            modifierIds = emptyList()
        )

        coVerify(exactly = 1) {
            productDao.updateProduct(match {
                it.name == "Kopi Susu Pandan" && it.createdAt == 123456789L
            })
        }
        coVerify(exactly = 1) { productDao.deleteProductModifierCrossRefs("prod-kopi") }
        coVerify(exactly = 0) { productDao.insertProductModifierCrossRefs(any()) }
    }

    @Test
    fun `saveModifier updates existing modifier or inserts new`() = runTest {
        coEvery { productDao.getModifierById("mod-extra-shot") } returns null
        repository.saveModifier(sampleModifier)
        coVerify(exactly = 1) { productDao.insertModifier(sampleModifier) }

        coEvery { productDao.getModifierById("mod-extra-shot") } returns sampleModifier
        val modified = sampleModifier.copy(price = 6000)
        repository.saveModifier(modified)
        coVerify(exactly = 1) { productDao.updateModifier(modified) }
    }

    @Test
    fun `seedDefaultModifiersIfEmpty seeds when count is zero`() = runTest {
        coEvery { productDao.countModifiers() } returns 0

        repository.seedDefaultModifiersIfEmpty()

        coVerify(exactly = 1) {
            productDao.insertModifiers(match { list ->
                list.isNotEmpty() && list.any { it.name.contains("Extra Espresso") }
            })
        }
    }

    @Test
    fun `seedDefaultModifiersIfEmpty does nothing when modifiers exist`() = runTest {
        coEvery { productDao.countModifiers() } returns 5

        repository.seedDefaultModifiersIfEmpty()

        coVerify(exactly = 0) { productDao.insertModifiers(any()) }
    }
}
