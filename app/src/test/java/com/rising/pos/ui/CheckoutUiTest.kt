package com.rising.pos.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rising.pos.core.database.entity.*
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.OrderType
import com.rising.pos.core.model.BusinessType
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.feature.pos.PosPhoneCatalog
import com.rising.pos.feature.pos.components.*
import com.rising.pos.ui.navigation.*
import com.rising.pos.ui.theme.RisingPosTheme
import com.rising.pos.ui.theme.CashierTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CheckoutUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val settings = BusinessSettings(name = "Warung Bu Siti")
    private fun photo(name: String) = File("src/test/resources/products/$name.${if (name == "tea") "png" else "jpg"}").absoluteFile.toURI().toString()
    private val products = listOf(
        ProductEntity(id = "mie", name = "Indomie Goreng", sellingPrice = 3500, stock = 50.0, imageUrl = photo("noodles")),
        ProductEntity(id = "minyak", name = "Minyak Goreng 1 L", sellingPrice = 20000, stock = 50.0, imageUrl = photo("oil")),
        ProductEntity(id = "gula", name = "Gula Pasir 1 kg", sellingPrice = 17000, stock = 50.0, imageUrl = photo("sugar")),
        ProductEntity(id = "teh", name = "Teh Botol 350 ml", sellingPrice = 5000, stock = 50.0, imageUrl = photo("tea")),
        ProductEntity(id = "beras", name = "Beras 5 kg", sellingPrice = 75000, stock = 50.0, imageUrl = photo("rice")),
        ProductEntity(id = "telur", name = "Telur Ayam 1 kg", sellingPrice = 28000, stock = 50.0, imageUrl = photo("eggs"))
    )
    private val cart = CartState(items = listOf(CartItem(product = products[0], quantity = 2.0), CartItem(product = products[1])))
    private val categories = listOf("Sembako", "Minuman", "Camilan").map { CategoryEntity(it, it) }
    @Composable
    private fun Catalog(onCart: () -> Unit = {}, onPay: () -> Unit = {}, onProduct: (ProductEntity) -> Unit = {}, onCategory: (String?) -> Unit = {}) {
        Column {
            Box(Modifier.weight(1f)) {
                PosPhoneCatalog(settings, products.map { ProductWithCategory(it, null) }, categories, cart, "", null, 0,
                    {}, {}, {}, onCategory, onProduct, {}, onCart, onPay)
            }
            PosBottomNavBar(phoneNavigationItems, "pos", {})
        }
    }
    @Test fun catalogHasSeparateCartAndPayActionsAndFourDestinations() {
        var opened = false; var paid = false; var added = ""; var selected: String? = null
        compose.setContent { RisingPosTheme(darkTheme = false) { Catalog({ opened = true }, { paid = true }, { added = it.id }, { selected = it }) } }
        awaitPhotos()
        compose.onNodeWithText("Rp28.000").assertIsDisplayed()
        savePreview("catalog")
        compose.onNodeWithContentDescription("Lihat keranjang").performClick(); assertTrue(opened)
        compose.onNodeWithText("Bayar Rp27.000").performClick(); assertTrue(paid)
        compose.onNodeWithContentDescription("Tambah Indomie Goreng").performClick(); assertEquals("mie", added)
        compose.onNodeWithText("Minuman").performClick(); assertEquals("Minuman", selected)
        compose.onNodeWithText("Lainnya").performClick()
        compose.onNodeWithText("Laporan").assertIsDisplayed()
        compose.onNodeWithText("Pengaturan").assertIsDisplayed()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test fun cartQuantityDeleteClearAndAddProductsWork() {
        var cleared = false; var addProducts = false; var changed = 0.0; var removed = ""
        compose.setContent { RisingPosTheme(darkTheme = false) { CashierTheme {
            Catalog()
            ModalBottomSheet(onDismissRequest = {}, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
                CartView(cart, settings, { _, delta -> changed = delta }, { removed = it }, { cleared = true }, {},
                    onAddProducts = { addProducts = true }, compact = true)
            }
        } } }
        awaitPhotos(); savePreview("cart")
        compose.onAllNodesWithContentDescription("Kurangi Indomie Goreng")[1].performClick(); assertEquals(-1.0, changed, 0.0)
        compose.onAllNodesWithContentDescription("Kurangi Minyak Goreng 1 L")[1].assertIsNotEnabled()
        compose.onNodeWithContentDescription("Hapus Indomie Goreng").performClick(); assertEquals(cart.items[0].cartItemId, removed)
        compose.onNodeWithText("Tambah barang").performScrollTo().performClick(); assertTrue(addProducts)
        compose.onNodeWithText("Kosongkan").performClick(); assertFalse(cleared)
        compose.onNodeWithText("Batal").performClick(); assertFalse(cleared)
        compose.onAllNodesWithText("Bayar Rp27.000")[1].assertIsDisplayed()
    }
    @Test fun paymentKeypadValidatesCashAndPresetsReplaceInput() {
        var paid = 0L
        compose.setContent { RisingPosTheme(darkTheme = false) {
            CheckoutDialog(cart, settings, false, null, {}, { method, amount, _, _ -> assertEquals(PaymentMethod.CASH, method); paid = amount })
        } }
        compose.onNodeWithText("Rp50.000").performClick()
        compose.onNodeWithText("Rp23.000").assertIsDisplayed()
        compose.onNodeWithTag("key_delete").assertIsDisplayed()
        savePreview("payment")
        compose.onNodeWithTag("key_1").performScrollTo().performClick()
        compose.onNodeWithTag("key_000").performScrollTo().performClick()
        compose.onNodeWithText("Uang kurang").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Selesaikan pembayaran").assertIsNotEnabled()
        compose.onNodeWithText("Uang pas").performScrollTo().performClick()
        compose.onNodeWithText("Selesaikan pembayaran").assertIsEnabled()
        compose.onNodeWithText("Rp50.000").performClick()
        compose.onNodeWithText("Selesaikan pembayaran").performClick(); assertEquals(50000L, paid)
    }
    @Test fun cafeDeliveryAndOrderNotesRemainAvailableInDetails() {
        var submittedType: OrderType? = null; var submittedNote: String? = null
        compose.setContent { RisingPosTheme(darkTheme = false) {
            CheckoutDialog(cart.copy(note = "Antar sore"), settings.copy(type = BusinessType.CAFE, isTableEnabled = false), false, null, {},
                { _, _, type, note -> submittedType = type; submittedNote = note })
        } }
        compose.onNodeWithText("Lihat rincian").performClick()
        compose.onNodeWithText("Nomor meja").assertDoesNotExist()
        compose.onNodeWithText("Diantar").performScrollTo().performClick()
        compose.onNodeWithText("QRIS").performScrollTo().performClick()
        compose.onNodeWithText("Selesaikan pembayaran").performClick()
        assertEquals(OrderType.DELIVERY, submittedType)
        assertEquals("Antar sore", submittedNote)
    }
    @Test fun processingBlocksPaymentAndNonCashUsesExactTotal() {
        var method: PaymentMethod? = null; var paid = 0L; var processing by mutableStateOf(false)
        compose.setContent { RisingPosTheme(darkTheme = false) {
            CheckoutDialog(cart, settings, processing, null, {}, { m, amount, _, _ -> method = m; paid = amount })
        } }
        compose.onNodeWithText("QRIS").performClick()
        compose.onNodeWithText("Selesaikan pembayaran").performClick()
        assertEquals(PaymentMethod.QRIS, method); assertEquals(27000L, paid)
        compose.runOnIdle { processing = true }
        compose.onNodeWithText("Tunai").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Kembali ke kasir").assertIsNotEnabled()
    }
    private fun awaitPhotos() {
        compose.waitUntil(10000) { compose.onAllNodesWithTag("loaded_Indomie Goreng", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("loaded_Telur Ayam 1 kg", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.waitForIdle()
    }
    private fun savePreview(name: String) {
        compose.runOnIdle {
            val dialog = ShadowDialog.getLatestDialog()?.takeIf { it.isShowing }
            val view = dialog?.window?.decorView ?: compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (dialog != null) compose.activity.window.decorView.draw(canvas)
            view.draw(canvas)
            val file = File("build/ui-previews/$name.png"); file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
