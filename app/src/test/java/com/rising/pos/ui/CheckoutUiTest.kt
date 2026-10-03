package com.rising.pos.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import org.robolectric.shadows.ShadowDialog
import java.io.File
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import com.rising.pos.feature.pos.components.CartView
import com.rising.pos.feature.pos.components.CheckoutDialog
import com.rising.pos.ui.theme.RisingPosTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CheckoutUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val settings = BusinessSettings(name = "Warung Bu Siti")
    private val cart = CartState(items = listOf(
        CartItem(product = ProductEntity(id = "mie", name = "Indomie Goreng", sellingPrice = 3500), quantity = 2.0),
        CartItem(product = ProductEntity(id = "minyak", name = "Minyak Goreng 1 L", sellingPrice = 20000))
    ))

    @Test fun clearCartRequiresConfirmationAndAddProductsReturnsToCatalog() {
        var cleared = false
        var addProducts = false
        compose.setContent {
            RisingPosTheme(darkTheme = false) {
                Surface {
                    CartView(cart, settings, { _, _ -> }, {}, { cleared = true }, {},
                        onAddProducts = { addProducts = true }, compact = true)
                }
            }
        }
        savePreview("cart")
        compose.onNodeWithText("Tambah barang").performScrollTo().performClick()
        assertTrue(addProducts)
        compose.onNodeWithText("Kosongkan").performClick()
        assertFalse(cleared)
        compose.onNodeWithText("Kosongkan keranjang?").assertIsDisplayed()
        compose.onNodeWithText("Batal").performClick()
        assertFalse(cleared)
        compose.onNodeWithText("Bayar").assertIsDisplayed()
    }

    @Test fun paymentRequiresSufficientCashAndQuickAmountReplacesInput() {
        var paid = 0L
        compose.setContent {
            RisingPosTheme(darkTheme = false) {
                CheckoutDialog(cart, settings, false, null, {}, { method, amount, _, _ ->
                    assertEquals(PaymentMethod.CASH, method)
                    paid = amount
                })
            }
        }
        savePreview("payment")
        compose.onNodeWithText("Nominal uang tunai").performScrollTo().performTextReplacement("10000")
        compose.onNodeWithText("Selesaikan pembayaran").assertIsNotEnabled()
        compose.onNodeWithText("Uang pas").performScrollTo().performClick()
        compose.onNodeWithText("Selesaikan pembayaran").assertIsEnabled()
        compose.onNodeWithText(CurrencyFormatter.format(50000L, settings.currencySymbol)).performScrollTo().performClick()
        compose.onNodeWithText("Nominal uang tunai").assertTextContains("50000")
        compose.onNodeWithText("Selesaikan pembayaran").performClick()
        assertEquals(50000L, paid)
    }
    private fun savePreview(name: String) {
        compose.runOnIdle {
            val dialog = ShadowDialog.getLatestDialog()?.takeIf { it.isShowing }
            val view = dialog?.window?.decorView ?: compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/ui-previews/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
