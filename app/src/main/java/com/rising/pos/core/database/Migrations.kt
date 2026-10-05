package com.rising.pos.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Kumpulan migrasi skema database.
 *
 * ATURAN: setiap kali `version` di [PosDatabase] dinaikkan, WAJIB menambahkan
 * [Migration] dari versi lama ke versi baru di sini dan mendaftarkannya ke
 * [ALL]. `fallbackToDestructiveMigration` SENGAJA TIDAK dipakai — tanpa itu,
 * lupa membuat migrasi akan membuat aplikasi gagal keras (mudah terdeteksi saat
 * testing) alih-alih menghapus seluruh data penjualan pengguna secara diam-diam.
 *
 * CATATAN: migrasi memakai `SupportSQLiteDatabase` dan hanya boleh berisi DDL/DML
 * murni. Untuk perubahan yang butuh logika (mis. mengisi nilai kolom baru),
 * lakukan via SQL `UPDATE`.
 */
object Migrations {

    /**
     * v1 → v2: mengubah seluruh kolom UANG dari `REAL` (Double) menjadi `INTEGER`
     * (Long, rupiah bulat) untuk menghindari galat pembulatan floating-point.
     * Kolom kuantitas/stok tetap `REAL`.
     *
     * SQLite tidak mendukung `ALTER TABLE ... ALTER COLUMN`, jadi tiap tabel
     * dibuat ulang: CREATE tabel baru → COPY data (dibulatkan) → DROP lama →
     * RENAME → buat ulang index. Room menjalankan migrasi dengan foreign_keys
     * OFF, sehingga urutan tabel tidak menimbulkan masalah FK.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // ── products: selling_price, cost_price → INTEGER ──────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `products_new` (`id` TEXT NOT NULL, `sku` TEXT, `barcode` TEXT, `name` TEXT NOT NULL, `category_id` TEXT, `description` TEXT, `selling_price` INTEGER NOT NULL, `cost_price` INTEGER NOT NULL, `stock` REAL NOT NULL, `unit` TEXT NOT NULL, `image_url` TEXT, `is_active` INTEGER NOT NULL, `track_stock` INTEGER NOT NULL, `min_stock` REAL NOT NULL, `is_favorite` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
            )
            db.execSQL(
                "INSERT INTO `products_new` (`id`,`sku`,`barcode`,`name`,`category_id`,`description`,`selling_price`,`cost_price`,`stock`,`unit`,`image_url`,`is_active`,`track_stock`,`min_stock`,`is_favorite`,`created_at`,`updated_at`,`sync_status`) " +
                    "SELECT `id`,`sku`,`barcode`,`name`,`category_id`,`description`,CAST(ROUND(`selling_price`) AS INTEGER),CAST(ROUND(`cost_price`) AS INTEGER),`stock`,`unit`,`image_url`,`is_active`,`track_stock`,`min_stock`,`is_favorite`,`created_at`,`updated_at`,`sync_status` FROM `products`"
            )
            db.execSQL("DROP TABLE `products`")
            db.execSQL("ALTER TABLE `products_new` RENAME TO `products`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_name` ON `products` (`name`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_category_id` ON `products` (`category_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_barcode` ON `products` (`barcode`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_is_favorite` ON `products` (`is_favorite`)")

            // ── product_variants: price_adjustment → INTEGER ───────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `product_variants_new` (`id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `name` TEXT NOT NULL, `price_adjustment` INTEGER NOT NULL, `sku` TEXT, `barcode` TEXT, `stock` REAL, PRIMARY KEY(`id`), FOREIGN KEY(`product_id`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL(
                "INSERT INTO `product_variants_new` (`id`,`product_id`,`name`,`price_adjustment`,`sku`,`barcode`,`stock`) " +
                    "SELECT `id`,`product_id`,`name`,CAST(ROUND(`price_adjustment`) AS INTEGER),`sku`,`barcode`,`stock` FROM `product_variants`"
            )
            db.execSQL("DROP TABLE `product_variants`")
            db.execSQL("ALTER TABLE `product_variants_new` RENAME TO `product_variants`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_product_variants_product_id` ON `product_variants` (`product_id`)")

            // ── modifiers: price → INTEGER ─────────────────────────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `modifiers_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `price` INTEGER NOT NULL, `is_required` INTEGER NOT NULL, `is_multiple_select` INTEGER NOT NULL, `is_active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO `modifiers_new` (`id`,`name`,`price`,`is_required`,`is_multiple_select`,`is_active`) " +
                    "SELECT `id`,`name`,CAST(ROUND(`price`) AS INTEGER),`is_required`,`is_multiple_select`,`is_active` FROM `modifiers`"
            )
            db.execSQL("DROP TABLE `modifiers`")
            db.execSQL("ALTER TABLE `modifiers_new` RENAME TO `modifiers`")

            // ── transactions: semua nominal → INTEGER ──────────────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transactions_new` (`id` TEXT NOT NULL, `receipt_number` TEXT NOT NULL, `device_id` TEXT NOT NULL, `cashier_id` TEXT, `customer_id` TEXT, `order_type` TEXT NOT NULL, `subtotal` INTEGER NOT NULL, `discount` INTEGER NOT NULL, `discount_reason` TEXT, `tax` INTEGER NOT NULL, `service_charge` INTEGER NOT NULL, `grand_total` INTEGER NOT NULL, `payment_amount` INTEGER NOT NULL, `change_amount` INTEGER NOT NULL, `payment_method` TEXT NOT NULL, `status` TEXT NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO `transactions_new` (`id`,`receipt_number`,`device_id`,`cashier_id`,`customer_id`,`order_type`,`subtotal`,`discount`,`discount_reason`,`tax`,`service_charge`,`grand_total`,`payment_amount`,`change_amount`,`payment_method`,`status`,`note`,`created_at`,`sync_status`) " +
                    "SELECT `id`,`receipt_number`,`device_id`,`cashier_id`,`customer_id`,`order_type`," +
                    "CAST(ROUND(`subtotal`) AS INTEGER),CAST(ROUND(`discount`) AS INTEGER),`discount_reason`," +
                    "CAST(ROUND(`tax`) AS INTEGER),CAST(ROUND(`service_charge`) AS INTEGER),CAST(ROUND(`grand_total`) AS INTEGER)," +
                    "CAST(ROUND(`payment_amount`) AS INTEGER),CAST(ROUND(`change_amount`) AS INTEGER)," +
                    "`payment_method`,`status`,`note`,`created_at`,`sync_status` FROM `transactions`"
            )
            db.execSQL("DROP TABLE `transactions`")
            db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_receipt_number` ON `transactions` (`receipt_number`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_created_at` ON `transactions` (`created_at`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_status` ON `transactions` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_customer_id` ON `transactions` (`customer_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sync_status` ON `transactions` (`sync_status`)")

            // ── transaction_items: unit_price, subtotal, discount → INTEGER ────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transaction_items_new` (`id` TEXT NOT NULL, `transaction_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `product_name` TEXT NOT NULL, `variant_name` TEXT, `qty` REAL NOT NULL, `unit_price` INTEGER NOT NULL, `subtotal` INTEGER NOT NULL, `discount` INTEGER NOT NULL, `note` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`transaction_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL(
                "INSERT INTO `transaction_items_new` (`id`,`transaction_id`,`product_id`,`product_name`,`variant_name`,`qty`,`unit_price`,`subtotal`,`discount`,`note`) " +
                    "SELECT `id`,`transaction_id`,`product_id`,`product_name`,`variant_name`,`qty`," +
                    "CAST(ROUND(`unit_price`) AS INTEGER),CAST(ROUND(`subtotal`) AS INTEGER),CAST(ROUND(`discount`) AS INTEGER),`note` FROM `transaction_items`"
            )
            db.execSQL("DROP TABLE `transaction_items`")
            db.execSQL("ALTER TABLE `transaction_items_new` RENAME TO `transaction_items`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_items_transaction_id` ON `transaction_items` (`transaction_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_items_product_id` ON `transaction_items` (`product_id`)")

            // ── transaction_item_modifiers: price → INTEGER ────────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transaction_item_modifiers_new` (`id` TEXT NOT NULL, `transaction_item_id` TEXT NOT NULL, `modifier_id` TEXT NOT NULL, `modifier_name` TEXT NOT NULL, `price` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`transaction_item_id`) REFERENCES `transaction_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL(
                "INSERT INTO `transaction_item_modifiers_new` (`id`,`transaction_item_id`,`modifier_id`,`modifier_name`,`price`) " +
                    "SELECT `id`,`transaction_item_id`,`modifier_id`,`modifier_name`,CAST(ROUND(`price`) AS INTEGER) FROM `transaction_item_modifiers`"
            )
            db.execSQL("DROP TABLE `transaction_item_modifiers`")
            db.execSQL("ALTER TABLE `transaction_item_modifiers_new` RENAME TO `transaction_item_modifiers`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_item_modifiers_transaction_item_id` ON `transaction_item_modifiers` (`transaction_item_id`)")

            // ── expenses: amount → INTEGER ─────────────────────────────────────
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `expenses_new` (`id` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT, `receipt_image_url` TEXT, `created_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO `expenses_new` (`id`,`category`,`amount`,`date`,`notes`,`receipt_image_url`,`created_at`,`sync_status`) " +
                    "SELECT `id`,`category`,CAST(ROUND(`amount`) AS INTEGER),`date`,`notes`,`receipt_image_url`,`created_at`,`sync_status` FROM `expenses`"
            )
            db.execSQL("DROP TABLE `expenses`")
            db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category` ON `expenses` (`category`)")
        }
    }

    /**
     * v2 → v3: menambahkan kolom `split_payment_method` dan `split_amount` pada
     * tabel `transactions` untuk mendukung pencatatan split payment yang terstruktur.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `split_payment_method` TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `split_amount` INTEGER NOT NULL DEFAULT 0")
        }
    }

    /** Daftar semua migrasi, dari versi terendah ke tertinggi. */
    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
    )
}
