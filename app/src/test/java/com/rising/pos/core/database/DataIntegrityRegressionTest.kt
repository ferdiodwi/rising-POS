package com.rising.pos.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rising.pos.core.database.entity.ProductEntity
import com.rising.pos.core.database.entity.StockMovementEntity
import com.rising.pos.core.database.entity.TransactionEntity
import com.rising.pos.core.model.StockMovementType
import com.rising.pos.core.model.PaymentMethod
import com.rising.pos.core.model.TransactionStatus
import com.rising.pos.data.repository.ProductRepositoryImpl
import com.rising.pos.data.repository.TransactionRepositoryImpl
import com.rising.pos.domain.model.CartItem
import com.rising.pos.domain.model.CartState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * Test regresi untuk 3 bug kehilangan data yang dilaporkan:
 *  1. Edit produk menghapus riwayat mutasi stok (FK CASCADE + INSERT OR REPLACE).
 *  2. Nomor pesanan tertunda yang bentrok menimpa pesanan lain (unique + REPLACE).
 *  3. Nomor urut memakai COUNT(*) sehingga bisa menabrak nomor yang sudah ada.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class DataIntegrityRegressionTest {

    private lateinit var db: PosDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PosDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── BUG #1: edit produk tidak boleh menghapus riwayat mutasi stok ──────────
    @Test
    fun `edit produk mempertahankan riwayat mutasi stok`() = runBlocking {
        val productId = "p-1"
        db.productDao().insertProduct(
            ProductEntity(id = productId, name = "Kopi", sellingPrice = 15000L, stock = 10.0)
        )
        db.stockMovementDao().insertMovement(
            StockMovementEntity(
                id = UUID.randomUUID().toString(),
                productId = productId,
                type = StockMovementType.SALE,
                qtyChange = -2.0,
                qtyBefore = 10.0,
                qtyAfter = 8.0,
                reason = "Penjualan awal"
            )
        )
        val before = db.stockMovementDao().getMovementsForProduct(productId).first()
        assertEquals(1, before.size)

        // Simulasi "edit produk" dari UI (nama/harga berubah, id sama).
        val repo = ProductRepositoryImpl(db.productDao(), db.categoryDao())
        repo.saveProduct(
            ProductEntity(id = productId, name = "Kopi Susu", sellingPrice = 18000L, stock = 10.0),
            emptyList()
        )

        val after = db.stockMovementDao().getMovementsForProduct(productId).first()
        assertEquals("Riwayat mutasi stok harus tetap ada setelah edit produk", 1, after.size)
        assertEquals("Kopi Susu", db.productDao().getProductById(productId)!!.name)
    }

    // ── BUG #2: nomor transaksi bentrok harus GAGAL, bukan menimpa ────────────
    @Test
    fun `nomor transaksi duplikat ditolak bukan menimpa`() = runBlocking {
        val trx = TransactionEntity(
            id = "t-1",
            receiptNumber = "HOLD-A01-20260930-0001",
            subtotal = 1000L,
            grandTotal = 1000L,
            paymentAmount = 1000L,
            status = TransactionStatus.HELD
        )
        db.transactionDao().insertTransaction(trx)
        assertEquals(1, db.transactionDao().getHeldTransactions().first().size)

        // Insert kedua dengan receipt_number sama tapi id berbeda.
        val dupe = trx.copy(id = "t-2")
        val failed = try {
            db.transactionDao().insertTransaction(dupe)
            false
        } catch (e: Exception) {
            true
        }
        assertTrue("Insert nomor duplikat harus melempar exception (ABORT)", failed)

        // Transaksi asli tetap ada dan TIDAK tertimpa.
        val held = db.transactionDao().getHeldTransactions().first()
        assertEquals("Pesanan tertunda asli harus tetap ada", 1, held.size)
        assertEquals("t-1", held.first().transaction.id)
    }

    // ── BUG #3: nomor urut pakai MAX, bukan COUNT (tahan terhadap penghapusan) ─
    @Test
    fun `nomor urut memakai max tidak menabrak setelah penghapusan`() = runBlocking {
        val prefix = "TRX-A01-20260930-"
        fun trx(id: String, no: Int) = TransactionEntity(
            id = id,
            receiptNumber = "$prefix${String.format("%05d", no)}",
            subtotal = 1000L,
            grandTotal = 1000L,
            paymentAmount = 1000L
        )
        db.transactionDao().insertTransaction(trx("t-1", 1))
        db.transactionDao().insertTransaction(trx("t-2", 2))
        db.transactionDao().insertTransaction(trx("t-3", 3))
        // Hapus nomor TENGAH (2), sisakan 1 dan 3.
        // COUNT(*) sekarang = 2 -> COUNT+1 = 3 -> MENABRAK nomor 3 yang masih ada.
        // MAX(...) = 3 -> MAX+1 = 4 -> aman.
        db.transactionDao().deleteTransaction("t-2")

        val next = db.transactionDao().nextReceiptSequence(prefix) + 1
        assertEquals("Nomor urut berikutnya harus 4 (MAX+1), bukan 3 (COUNT+1)", 4, next)
    }

    // ── BUG #4: checkout melebihi stok harus DITOLAK (cegah stok negatif) ──────
    @Test
    fun `checkout melebihi stok ditolak dan stok tidak negatif`() = runBlocking {
        val productId = "p-stok"
        db.productDao().insertProduct(
            ProductEntity(id = productId, name = "Gula", sellingPrice = 12000L, stock = 5.0, trackStock = true)
        )
        val product = db.productDao().getProductById(productId)!!
        val repo = TransactionRepositoryImpl(
            db, db.transactionDao(), db.productDao(), db.stockMovementDao()
        )

        val cart = CartState(
            items = listOf(CartItem(product = product, quantity = 10.0)) // minta 10, stok cuma 5
        )

        val result = repo.processCheckout(
            cartState = cart,
            paymentMethod = PaymentMethod.CASH,
            paymentAmount = 1_000_000L,
            deviceId = "A01",
            cashierId = "Kasir",
            isTaxEnabled = false,
            taxPercentage = 0.0,
            isTaxInclusive = true,
            isServiceChargeEnabled = false,
            serviceChargePercentage = 0.0
        )

        assertTrue("Checkout melebihi stok harus gagal", result.isFailure)
        // Stok tidak boleh berubah / negatif.
        assertEquals(5.0, db.productDao().getProductById(productId)!!.stock, 0.001)
        // Tidak ada transaksi yang tercatat (rollback penuh).
        assertEquals(0, db.transactionDao().getRecentTransactions().first().size)
    }
}
