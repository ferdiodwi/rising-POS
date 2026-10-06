package com.rising.pos.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.rising.pos.core.database.entity.*
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.domain.model.*
import com.rising.pos.feature.pos.*
import com.rising.pos.feature.pos.components.TabletCartView
import com.rising.pos.ui.components.PosSearchBar
import com.rising.pos.ui.navigation.*
import com.rising.pos.ui.theme.RisingPosTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w1024dp-h768dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabletCashierUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val settings = BusinessSettings(name = "Rising Studio")
    private val products = listOf(
        Triple("Indomie Goreng", 3500L, "noodles.jpg"),
        Triple("Minyak Goreng 1 L", 20000L, "oil.jpg"),
        Triple("Gula Pasir 1 kg", 17000L, "sugar.jpg"),
        Triple("Teh Botol 350 ml", 5000L, "tea.png"),
        Triple("Beras 5 kg", 75000L, "rice.jpg"),
        Triple("Telur Ayam 1 kg", 28000L, "eggs.jpg")
    ).mapIndexed { index, (name, price, image) ->
        ProductEntity(id = "$index", name = name, sellingPrice = price, stock = 50.0,
            imageUrl = File("src/test/resources/products/$image").absoluteFile.toURI().toString())
    }
    private val initialCart = CartState(items = listOf(
        CartItem(product = products[0], quantity = 2.0), CartItem(product = products[1])
    ))
    private val categories = listOf("Makanan", "Minuman", "Camilan / Snack").map { CategoryEntity(it, it) }

    @Composable
    private fun Tablet(cart: CartState, onUpdate: (String, Double) -> Unit = { _, _ -> },
        onClear: () -> Unit = {}, onPay: () -> Unit = {}, onCustomer: () -> Unit = {}) {
        Column {
            Box(Modifier.weight(1f)) {
                PosTabletLayout(
                    header = { PosToolbar(settings.name, 0, 0, false, {}, {}) },
                    catalog = { columns ->
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(12.dp))
                            PosSearchBar("", {}, "Cari nama atau kode barang", trailingBarcodeAction = {})
                            Spacer(Modifier.height(8.dp))
                            CategoryFilters(categories, null, {}, edgePadding = 0.dp)
                            Spacer(Modifier.height(10.dp))
                            ProductGrid(products.map { ProductWithCategory(it, null) }, "Rp", cart.items,
                                {}, {}, columns, false, modifier = Modifier.weight(1f), tablet = true)
                        }
                    },
                    cart = {
                        TabletCartView(cart, settings, onUpdate, {}, onClear, onPay, {}, onCustomer, {}, {}, {}, {})
                    }
                )
            }
            PosBottomNavBar(phoneNavigationItems, Screen.Pos.route, {}, expanded = true)
        }
    }

    @Test fun tabletShowsReferenceLayoutAndUsesCartCallbacks() {
        var cart by mutableStateOf(initialCart)
        var paid = false
        var customer = false
        compose.setContent { RisingPosTheme(darkTheme = false) {
            Tablet(cart, onUpdate = { id, delta ->
                cart = cart.copy(items = cart.items.map { if (it.cartItemId == id) it.copy(quantity = it.quantity + delta) else it })
            }, onPay = { paid = true }, onCustomer = { customer = true })
        } }
        compose.onNodeWithText("3 barang • 2 produk").assertIsDisplayed()
        compose.onNodeWithText("Bayar Rp27.000").assertIsDisplayed()
        compose.onNodeWithText("Rp28.000").assertIsDisplayed()
        compose.onNodeWithText("Pilih pelanggan").performClick()
        assertTrue(customer)
        compose.waitUntil(10000) {
            compose.onAllNodesWithTag("loaded_Telur Ayam 1 kg", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        savePreview("tablet-cashier")
        compose.onAllNodesWithContentDescription("Tambah Indomie Goreng")[1].performClick()
        compose.onNodeWithText("Bayar Rp30.500").assertIsDisplayed().performClick()
        assertTrue(paid)
        assertEquals(3.0, cart.items.first().quantity, 0.0)
    }

    @Test
    @Config(qualifiers = "w600dp-h960dp-mdpi")
    fun narrowTabletKeepsQuantityAndCheckoutAccessible() {
        var delta = 0.0
        compose.setContent { RisingPosTheme(darkTheme = false) {
            Tablet(initialCart, onUpdate = { _, amount -> delta = amount })
        } }
        compose.onNodeWithText("Bayar Rp27.000").assertIsDisplayed()
        compose.onNodeWithContentDescription("Hapus Indomie Goreng").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("Tambah Indomie Goreng")[1].assertIsDisplayed().performClick()
        assertEquals(1.0, delta, 0.0)
        savePreview("tablet-cashier-600")
    }

    @Test fun emptyTabletDisablesCheckoutAndClearRequiresConfirmation() {
        var cart by mutableStateOf(initialCart)
        compose.setContent { RisingPosTheme(darkTheme = false) {
            Tablet(cart, onClear = { cart = CartState() })
        } }
        compose.onNodeWithText("Kosongkan").performClick()
        assertEquals(2, cart.items.size)
        compose.onNodeWithText("Batal").performClick()
        assertEquals(2, cart.items.size)
        compose.onNodeWithText("Kosongkan").performClick()
        compose.onAllNodesWithText("Kosongkan")[1].performClick()
        compose.onNodeWithText("Keranjang masih kosong").assertIsDisplayed()
        compose.onNodeWithText("Bayar Rp0").assertIsNotEnabled()
    }

    @Test
    @Config(qualifiers = "w850dp-h393dp-mdpi")
    fun actualLandscapeScreenUsesFullHeaderAndBottomNavigation() {
        val model = mockk<PosViewModel>(relaxed = true)
        every { model.uiState } returns MutableStateFlow(PosUiState(cart = initialCart))
        every { model.settings } returns MutableStateFlow(settings)
        every { model.categories } returns MutableStateFlow(categories)
        every { model.filteredProducts } returns MutableStateFlow(products.map { ProductWithCategory(it, null) })
        every { model.heldTransactions } returns MutableStateFlow(emptyList())
        every { model.customers } returns MutableStateFlow(emptyList())
        every { model.tables } returns MutableStateFlow(emptyList())
        var route = ""
        compose.setContent { RisingPosTheme(darkTheme = false) {
            PosAppScaffold(Screen.Pos.route, { route = it }) { PosScreen(model) }
        } }
        compose.onNodeWithText("Rising Studio").assertIsDisplayed()
        compose.onNodeWithText("Cari nama atau kode barang").assertIsDisplayed()
        compose.onNodeWithText("3 barang • 2 produk").assertIsDisplayed()
        compose.onNodeWithText("Bayar Rp27.000").assertIsDisplayed().performClick()
        verify { model.openCheckoutDialog() }
        val cartBounds = compose.onNodeWithText("Keranjang").fetchSemanticsNode().boundsInRoot
        val navBounds = compose.onNodeWithText("Pengaturan").fetchSemanticsNode().boundsInRoot
        assertTrue("Navigation must be below the cashier", navBounds.top > cartBounds.bottom)
        compose.onNodeWithText("Pengaturan").performClick()
        assertEquals(Screen.Settings.route, route)
        compose.waitUntil(10000) {
            compose.onAllNodesWithTag("loaded_Indomie Goreng", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        savePreview("cashier-phone-landscape")
        compose.onNodeWithTag("tablet_cart_items").performScrollToNode(hasText("Subtotal"))
        compose.onNodeWithText("Subtotal").assertIsDisplayed()
        compose.onNodeWithTag("tablet_cart_items").performScrollToNode(hasText("Tambah diskon"))
        compose.onNodeWithText("Tambah diskon").assertIsDisplayed()
        compose.onNodeWithText("Bayar Rp27.000").assertIsDisplayed()
    }

    private fun savePreview(name: String) {
        compose.waitForIdle()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/ui-previews/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
