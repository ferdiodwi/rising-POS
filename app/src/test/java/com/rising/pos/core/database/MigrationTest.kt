package com.rising.pos.core.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Memverifikasi migrasi v1 → v2 (kolom uang REAL → INTEGER).
 *
 * Test ini menggerakkan [Migrations] secara langsung: membuat database versi 1
 * dengan skema asli, mengisi data (termasuk nilai pecahan), menjalankan migrasi,
 * lalu memeriksa hasilnya. Pendekatan manual dipilih agar tidak bergantung pada
 * penyediaan berkas schema Room sebagai aset test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MigrationTest {

    private lateinit var context: Context
    private lateinit var helper: SupportSQLiteOpenHelper
    private val dbName = "migration-test.db"

    /** Skema v1 apa adanya (kolom uang = REAL), dibaca dari schema 1.json. */
    private val v1Schema = listOf(
        "CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT, `sort_order` INTEGER NOT NULL, `is_active` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)",
        "CREATE INDEX IF NOT EXISTS `index_categories_sort_order` ON `categories` (`sort_order`)",
        "CREATE TABLE IF NOT EXISTS `products` (`id` TEXT NOT NULL, `sku` TEXT, `barcode` TEXT, `name` TEXT NOT NULL, `category_id` TEXT, `description` TEXT, `selling_price` REAL NOT NULL, `cost_price` REAL NOT NULL, `stock` REAL NOT NULL, `unit` TEXT NOT NULL, `image_url` TEXT, `is_active` INTEGER NOT NULL, `track_stock` INTEGER NOT NULL, `min_stock` REAL NOT NULL, `is_favorite` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        "CREATE INDEX IF NOT EXISTS `index_products_name` ON `products` (`name`)",
        "CREATE INDEX IF NOT EXISTS `index_products_category_id` ON `products` (`category_id`)",
        "CREATE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)",
        "CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)",
        "CREATE INDEX IF NOT EXISTS `index_products_is_favorite` ON `products` (`is_favorite`)",
        "CREATE TABLE IF NOT EXISTS `product_variants` (`id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `name` TEXT NOT NULL, `price_adjustment` REAL NOT NULL, `sku` TEXT, `barcode` TEXT, `stock` REAL, PRIMARY KEY(`id`), FOREIGN KEY(`product_id`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_product_variants_product_id` ON `product_variants` (`product_id`)",
        "CREATE TABLE IF NOT EXISTS `modifiers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `is_required` INTEGER NOT NULL, `is_multiple_select` INTEGER NOT NULL, `is_active` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `product_modifier_cross_ref` (`product_id` TEXT NOT NULL, `modifier_id` TEXT NOT NULL, PRIMARY KEY(`product_id`, `modifier_id`), FOREIGN KEY(`product_id`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`modifier_id`) REFERENCES `modifiers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_product_modifier_cross_ref_product_id` ON `product_modifier_cross_ref` (`product_id`)",
        "CREATE INDEX IF NOT EXISTS `index_product_modifier_cross_ref_modifier_id` ON `product_modifier_cross_ref` (`modifier_id`)",
        "CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `receipt_number` TEXT NOT NULL, `device_id` TEXT NOT NULL, `cashier_id` TEXT, `customer_id` TEXT, `order_type` TEXT NOT NULL, `subtotal` REAL NOT NULL, `discount` REAL NOT NULL, `discount_reason` TEXT, `tax` REAL NOT NULL, `service_charge` REAL NOT NULL, `grand_total` REAL NOT NULL, `payment_amount` REAL NOT NULL, `change_amount` REAL NOT NULL, `payment_method` TEXT NOT NULL, `status` TEXT NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_receipt_number` ON `transactions` (`receipt_number`)",
        "CREATE INDEX IF NOT EXISTS `index_transactions_created_at` ON `transactions` (`created_at`)",
        "CREATE INDEX IF NOT EXISTS `index_transactions_status` ON `transactions` (`status`)",
        "CREATE INDEX IF NOT EXISTS `index_transactions_customer_id` ON `transactions` (`customer_id`)",
        "CREATE INDEX IF NOT EXISTS `index_transactions_sync_status` ON `transactions` (`sync_status`)",
        "CREATE TABLE IF NOT EXISTS `transaction_items` (`id` TEXT NOT NULL, `transaction_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `product_name` TEXT NOT NULL, `variant_name` TEXT, `qty` REAL NOT NULL, `unit_price` REAL NOT NULL, `subtotal` REAL NOT NULL, `discount` REAL NOT NULL, `note` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`transaction_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_transaction_items_transaction_id` ON `transaction_items` (`transaction_id`)",
        "CREATE INDEX IF NOT EXISTS `index_transaction_items_product_id` ON `transaction_items` (`product_id`)",
        "CREATE TABLE IF NOT EXISTS `transaction_item_modifiers` (`id` TEXT NOT NULL, `transaction_item_id` TEXT NOT NULL, `modifier_id` TEXT NOT NULL, `modifier_name` TEXT NOT NULL, `price` REAL NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`transaction_item_id`) REFERENCES `transaction_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_transaction_item_modifiers_transaction_item_id` ON `transaction_item_modifiers` (`transaction_item_id`)",
        "CREATE TABLE IF NOT EXISTS `stock_movements` (`id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `type` TEXT NOT NULL, `qty_change` REAL NOT NULL, `qty_before` REAL NOT NULL, `qty_after` REAL NOT NULL, `reason` TEXT, `reference_id` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`product_id`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_stock_movements_product_id` ON `stock_movements` (`product_id`)",
        "CREATE INDEX IF NOT EXISTS `index_stock_movements_created_at` ON `stock_movements` (`created_at`)",
        "CREATE INDEX IF NOT EXISTS `index_stock_movements_reference_id` ON `stock_movements` (`reference_id`)",
        "CREATE INDEX IF NOT EXISTS `index_stock_movements_type` ON `stock_movements` (`type`)",
        "CREATE TABLE IF NOT EXISTS `customers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `email` TEXT, `address` TEXT, `notes` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_customers_name` ON `customers` (`name`)",
        "CREATE INDEX IF NOT EXISTS `index_customers_phone` ON `customers` (`phone`)",
        "CREATE TABLE IF NOT EXISTS `expenses` (`id` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT, `receipt_image_url` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)",
        "CREATE INDEX IF NOT EXISTS `index_expenses_category` ON `expenses` (`category`)",
        "CREATE TABLE IF NOT EXISTS `restaurant_tables` (`id` TEXT NOT NULL, `table_number` TEXT NOT NULL, `capacity` INTEGER NOT NULL, `is_occupied` INTEGER NOT NULL, `current_transaction_id` TEXT, PRIMARY KEY(`id`))",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_restaurant_tables_table_number` ON `restaurant_tables` (`table_number`)",
        "CREATE TABLE IF NOT EXISTS `sync_queue` (`id` TEXT NOT NULL, `entity_type` TEXT NOT NULL, `entity_id` TEXT NOT NULL, `action` TEXT NOT NULL, `payload_json` TEXT NOT NULL, `device_id` TEXT NOT NULL, `status` TEXT NOT NULL, `retry_count` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_sync_queue_status` ON `sync_queue` (`status`)",
        "CREATE INDEX IF NOT EXISTS `index_sync_queue_created_at` ON `sync_queue` (`created_at`)"
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
        helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        v1Schema.forEach { db.execSQL(it) }
                        db.execSQL(
                            "INSERT INTO products (id, name, selling_price, cost_price, stock, unit, is_active, track_stock, min_stock, is_favorite, created_at, updated_at, sync_status) " +
                                "VALUES ('p1', 'Kopi', 15000.4, 10000.6, 10.5, 'pcs', 1, 1, 5.0, 0, 1, 1, 'PENDING')"
                        )
                        db.execSQL(
                            "INSERT INTO transactions (id, receipt_number, device_id, order_type, subtotal, discount, tax, service_charge, grand_total, payment_amount, change_amount, payment_method, status, created_at, sync_status) " +
                                "VALUES ('t1', 'TRX-A01-20260101-00001', 'A01', 'RETAIL', 15000.4, 0.0, 0.0, 0.0, 15000.4, 20000.0, 4999.6, 'CASH', 'COMPLETED', 1, 'PENDING')"
                        )
                        db.execSQL(
                            "INSERT INTO transaction_items (id, transaction_id, product_id, product_name, qty, unit_price, subtotal, discount) " +
                                "VALUES ('i1', 't1', 'p1', 'Kopi', 2.5, 15000.4, 37501.0, 0.0)"
                        )
                        db.execSQL(
                            "INSERT INTO expenses (id, category, amount, date, created_at, sync_status) " +
                                "VALUES ('e1', 'Operasional', 50000.7, 1, 1, 'PENDING')"
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
    }

    @After
    fun tearDown() {
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate1To2_convertsMoneyToIntegerAndKeepsQty() {
        val db = helper.writableDatabase

        // Jalankan migrasi v1 -> v2.
        Migrations.ALL.first().migrate(db)
        db.execSQL("PRAGMA user_version = 2")

        // Harga dibulatkan ke rupiah terdekat.
        db.query("SELECT selling_price, cost_price, stock FROM products WHERE id='p1'").use { c ->
            c.moveToFirst()
            assertEquals("selling_price 15000.4 → 15000", 15000L, c.getLong(0))
            assertEquals("cost_price 10000.6 → 10001", 10001L, c.getLong(1))
            assertEquals("stock tetap REAL", 10.5, c.getDouble(2), 0.0001)
        }

        db.query("SELECT subtotal, grand_total, payment_amount, change_amount FROM transactions WHERE id='t1'").use { c ->
            c.moveToFirst()
            assertEquals(15000L, c.getLong(0))
            assertEquals(15000L, c.getLong(1))
            assertEquals(20000L, c.getLong(2))
            assertEquals("change 4999.6 → 5000", 5000L, c.getLong(3))
        }

        db.query("SELECT qty, unit_price, subtotal FROM transaction_items WHERE id='i1'").use { c ->
            c.moveToFirst()
            assertEquals("qty tetap REAL", 2.5, c.getDouble(0), 0.0001)
            assertEquals(15000L, c.getLong(1))
            assertEquals(37501L, c.getLong(2))
        }

        db.query("SELECT amount FROM expenses WHERE id='e1'").use { c ->
            c.moveToFirst()
            assertEquals("amount 50000.7 → 50001", 50001L, c.getLong(0))
        }
    }

    @Test
    fun migrate1To2_preservesRowCounts() {
        val db = helper.writableDatabase
        Migrations.ALL.first().migrate(db)

        listOf("products", "transactions", "transaction_items", "expenses").forEach { table ->
            db.query("SELECT COUNT(*) FROM $table").use { c ->
                c.moveToFirst()
                assertEquals("Tabel $table tidak boleh kehilangan data", 1, c.getInt(0))
            }
        }
    }

    @Test
    fun migrate1To2_createsExpectedIndexes() {
        val db = helper.writableDatabase
        Migrations.ALL.first().migrate(db)

        db.query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_%'").use { c ->
            val names = buildSet {
                while (c.moveToNext()) add(c.getString(0))
            }
            assertTrue("Index unique receipt_number harus ada", "index_transactions_receipt_number" in names)
            assertTrue("Index products name harus ada", "index_products_name" in names)
            assertTrue("Index expenses date harus ada", "index_expenses_date" in names)
        }
    }
}
