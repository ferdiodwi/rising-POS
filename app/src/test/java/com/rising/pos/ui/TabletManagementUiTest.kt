package com.rising.pos.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rising.pos.core.database.entity.*
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.*
import com.rising.pos.core.update.model.UpdateState
import com.rising.pos.feature.product.*
import com.rising.pos.feature.transaction.*
import com.rising.pos.feature.dashboard.*
import com.rising.pos.feature.settings.*
import com.rising.pos.ui.navigation.*
import com.rising.pos.ui.theme.RisingPosTheme
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w1024dp-h768dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabletManagementUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val settings = BusinessSettings(name = "Rising Studio", email = "shop@example.com")
    private val categories = listOf("Makanan", "Minuman", "Sembako", "Camilan").map { CategoryEntity(it, it) }
    private val products = listOf(
        Triple("Indomie Goreng", 3500L, "noodles.jpg"), Triple("Minyak Goreng 1 L", 20000L, "oil.jpg"),
        Triple("Gula Pasir 1 kg", 17000L, "sugar.jpg"), Triple("Teh Botol 350 ml", 5000L, "tea.png"),
        Triple("Beras 5 kg", 75000L, "rice.jpg"), Triple("Telur Ayam 1 kg", 28000L, "eggs.jpg"),
        Triple("Produk halaman kedua", 12000L, "rice.jpg")
    ).mapIndexed { i, (name, price, image) ->
        ProductWithCategory(ProductEntity(id = "$i", sku = "PRD00$i", name = name, sellingPrice = price,
            stock = if (i == 2 || i == 5) 2.5 else 48.0,
            imageUrl = File("src/test/resources/products/$image").absoluteFile.toURI().toString()),
            categories[if (i == 0) 0 else if (i == 3) 1 else 2])
    }
    private val transactions = (1..7).map { index ->
        TransactionWithDetails(TransactionEntity(id = "$index", receiptNumber = "TRX-A01-20261006-0000$index",
            subtotal = 27000, grandTotal = 27000, paymentAmount = 50000, changeAmount = 23000,
            status = if (index == 3) TransactionStatus.CANCELLED else TransactionStatus.COMPLETED),
            listOf(TransactionItemWithModifiers(TransactionItemEntity(id = "line$index", transactionId = "$index",
                productId = "0", productName = "Indomie Goreng", qty = 2.0, unitPrice = 3500, subtotal = 7000), emptyList()),
                TransactionItemWithModifiers(TransactionItemEntity(id = "oil$index", transactionId = "$index",
                    productId = "1", productName = "Minyak Goreng 1 L", qty = 1.0, unitPrice = 20000, subtotal = 20000), emptyList())))
    }

    private fun productModel(): ProductViewModel = mockk<ProductViewModel>(relaxed = true).also { model ->
        every { model.settings } returns MutableStateFlow(settings)
        every { model.uiState } returns MutableStateFlow(ProductUiState())
        every { model.categories } returns MutableStateFlow(categories)
        every { model.modifiers } returns MutableStateFlow(emptyList())
        every { model.filteredProducts } returns MutableStateFlow(products)
    }
    private fun historyModel(): TransactionViewModel = mockk<TransactionViewModel>(relaxed = true).also { model ->
        every { model.settings } returns MutableStateFlow(settings)
        every { model.uiState } returns MutableStateFlow(TransactionUiState())
        every { model.period } returns MutableStateFlow(HistoryPeriod.TODAY)
        every { model.customDate } returns MutableStateFlow(null)
        every { model.searchQuery } returns MutableStateFlow("")
        every { model.transactions } returns MutableStateFlow(transactions)
        every { model.filteredTransactions } returns MutableStateFlow(transactions)
    }
    private fun reportModel(): DashboardViewModel = mockk<DashboardViewModel>(relaxed = true).also { model ->
        every { model.settings } returns MutableStateFlow(settings)
        every { model.selectedPeriod } returns MutableStateFlow(DashboardPeriod.LAST_7_DAYS)
        every { model.isPrinting } returns MutableStateFlow(false)
        every { model.printMessage } returns MutableStateFlow(null)
        every { model.metrics } returns MutableStateFlow(DashboardMetrics(grossSales = 8750000, transactionCount = 223,
            averageTicketSize = 39238, dailyAverageSales = 1250000, dateRangeText = "30 Sep – 6 Okt 2026",
            dailySales = listOf(980000L, 1050000L, 920000L, 1200000L, 1750000L, 1600000L, 1250000L).mapIndexed { i, amount ->
                SalesPoint("${i + 1}", amount, dayName = listOf("Rab", "Kam", "Jum", "Sab", "Min", "Sen", "Sel")[i])
            }, topProductsDisplay = products.take(5).mapIndexed { i, p ->
                TopProductDisplayItem(i + 1, p.product.id, p.product.name, p.category!!.name, p.product.imageUrl, "${86 - i * 10} pcs")
            }))
    }
    private fun settingsModel(): SettingsViewModel = mockk<SettingsViewModel>(relaxed = true).also { model ->
        every { model.settings } returns MutableStateFlow(settings)
        every { model.uiState } returns MutableStateFlow(SettingsUiState())
        every { model.updateState } returns MutableStateFlow(UpdateState.Idle)
        every { model.tables } returns MutableStateFlow(emptyList())
        every { model.syncPendingCount } returns MutableStateFlow(0)
    }
    private fun page(route: Screen, content: @Composable () -> Unit) {
        compose.setContent { RisingPosTheme(darkTheme = false) { PosAppScaffold(route.route, {}, content) } }
    }
    @Test fun productsSelectEditAndPaginateWithMatchingDetail() {
        val model = productModel()
        var adjusted = false
        page(Screen.Products) { ProductScreen(model, { adjusted = true }) }
        compose.onNodeWithText("Detail produk").assertIsDisplayed()
        savePreview("products-tablet")
        compose.onNodeWithTag("product_row_1").performClick()
        compose.onNodeWithText("Edit produk").performClick()
        verify { model.openEditProductForm(products[1]) }
        compose.onNodeWithText("Sesuaikan stok").performClick()
        assertTrue(adjusted)
        compose.onNodeWithContentDescription("Halaman berikutnya").performClick()
        compose.onNodeWithTag("product_row_6").assertExists()
        compose.onNodeWithText("Edit produk").performClick()
        verify { model.openEditProductForm(products[6]) }
    }
    @Test fun historyUsesActualReceiptAndPrintsSelectedTransaction() {
        val model = historyModel()
        page(Screen.Transactions) { TransactionScreen(model) }
        compose.onNodeWithText("Detail transaksi").assertIsDisplayed()
        savePreview("history-tablet")
        compose.onNodeWithTag("history_row_2").performClick()
        compose.onNodeWithText("Cetak struk").performClick()
        verify { model.printReceipt(transactions[1]) }
        compose.onNodeWithText("7 hari").performClick()
        verify { model.setPeriod(HistoryPeriod.LAST_7_DAYS) }
        compose.onNodeWithContentDescription("Halaman berikutnya").performClick()
        compose.onNodeWithText("Cetak struk").performClick()
        verify { model.printReceipt(transactions[6]) }
    }
    @Test fun reportUsesThreeStatsTrendAndProductNavigation() {
        val model = reportModel()
        var opened = false
        page(Screen.Dashboard) { DashboardScreen(model, onNavigateToProducts = { opened = true }) }
        compose.onNodeWithText("Total penjualan").assertIsDisplayed()
        compose.onNodeWithText("Rp8.750.000").assertIsDisplayed()
        compose.onNodeWithText("Produk terlaris").assertIsDisplayed()
        savePreview("reports-tablet")
        compose.onNodeWithText("Hari ini").performClick()
        verify { model.setPeriod(DashboardPeriod.TODAY) }
        compose.onNodeWithText("Lihat semua produk").performScrollTo().performClick()
        assertTrue(opened)
    }
    @Test fun storeProfileSavesEmailAndCancelRestoresFields() {
        val model = settingsModel()
        page(Screen.Settings) { SettingsScreen(model) }
        compose.onNodeWithText("Simpan perubahan").assertIsDisplayed()
        savePreview("settings-tablet")
        compose.onNode(hasSetTextAction() and hasText("Rising Studio")).performTextReplacement("Toko Baru")
        compose.onNodeWithText("Batal").performClick()
        compose.onNode(hasSetTextAction() and hasText("Rising Studio")).assertExists()
        compose.onNode(hasSetTextAction() and hasText("Rising Studio")).performTextReplacement("Toko Baru")
        compose.onNodeWithText("Simpan perubahan").performClick()
        verify { model.updateBusinessProfile("Toko Baru", "", "", settings.footerNote, "A01", "shop@example.com", "") }
    }
    @Test @Config(qualifiers = "w850dp-h393dp-mdpi")
    fun landscapeProductsKeepDetailAndActionsAccessible() {
        val model = productModel()
        page(Screen.Products) { ProductScreen(model) }
        compose.onNodeWithText("Detail produk").assertIsDisplayed()
        compose.onNodeWithText("Edit produk").assertIsDisplayed().performClick()
        verify { model.openEditProductForm(products[0]) }
        compose.onNodeWithContentDescription("Halaman berikutnya").assertIsDisplayed()
        savePreview("products-landscape")
    }
    @Test @Config(qualifiers = "w850dp-h393dp-mdpi")
    fun landscapeHistoryKeepsPrintAndShareVisible() {
        val model = historyModel()
        page(Screen.Transactions) { TransactionScreen(model) }
        compose.onNodeWithText("Detail transaksi").assertIsDisplayed()
        compose.onNodeWithText("Cetak struk").assertIsDisplayed().performClick()
        compose.onNodeWithText("Bagikan struk").assertIsDisplayed()
        verify { model.printReceipt(transactions[0]) }
        savePreview("history-landscape")
    }
    @Test @Config(qualifiers = "w850dp-h393dp-mdpi")
    fun landscapeReportKeepsTabletStatsAndScrollableChart() {
        val model = reportModel()
        page(Screen.Dashboard) { DashboardScreen(model) }
        compose.onNodeWithText("Total penjualan").assertExists()
        compose.onNodeWithText("Produk terlaris").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ekspor laporan").assertIsDisplayed()
        savePreview("reports-landscape")
    }
    @Test @Config(qualifiers = "w850dp-h393dp-mdpi")
    fun landscapeSettingsKeepsSidebarAndSaveVisible() {
        val model = settingsModel()
        page(Screen.Settings) { SettingsScreen(model) }
        compose.onNodeWithText("Simpan perubahan").assertIsDisplayed()
        compose.onNodeWithText("Metode pembayaran").assertIsDisplayed().performClick()
        compose.onNodeWithText("Metode pembayaran yang tersedia di kasir.").assertIsDisplayed()
        savePreview("settings-landscape")
    }
    private fun savePreview(name: String) {
        compose.waitForIdle()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/ui-previews/$name.png"); file.parentFile?.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
