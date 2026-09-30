package com.rising.pos.core.export

import com.rising.pos.core.database.entity.ExpenseEntity
import com.rising.pos.core.database.entity.ProductWithCategory
import com.rising.pos.core.database.entity.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun escape(value: Any?): String {
        if (value == null) return ""
        val str = value.toString().replace("\"", "\"\"")
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"$str\""
        } else {
            str
        }
    }

    fun generateTransactionsCsv(
        transactions: List<TransactionWithDetails>,
        currencySymbol: String = "Rp"
    ): String {
        val sb = StringBuilder()
        // CSV Header
        sb.appendLine(
            listOf(
                "No. Struk",
                "Waktu Transaksi",
                "Kasir",
                "Tipe Pesanan",
                "Status",
                "Metode Pembayaran",
                "Subtotal ($currencySymbol)",
                "Diskon ($currencySymbol)",
                "Alasan Diskon",
                "Pajak ($currencySymbol)",
                "Layanan ($currencySymbol)",
                "Total Akhir ($currencySymbol)",
                "Uang Diterima ($currencySymbol)",
                "Kembalian ($currencySymbol)",
                "Catatan",
                "Total Item",
                "Daftar Produk"
            ).joinToString(",")
        )

        for (item in transactions) {
            val trx = item.transaction
            val itemsSummary = item.items.joinToString(" | ") { detail ->
                val itm = detail.item
                "${itm.productName} (x${itm.qty.toInt()}) @${itm.unitPrice.toInt()}"
            }

            sb.appendLine(
                listOf(
                    escape(trx.receiptNumber),
                    escape(dateTimeFormat.format(Date(trx.createdAt))),
                    escape(trx.cashierId ?: "Admin"),
                    escape(trx.orderType.name),
                    escape(trx.status.name),
                    escape(trx.paymentMethod.name),
                    escape(trx.subtotal),
                    escape(trx.discount),
                    escape(trx.discountReason ?: "-"),
                    escape(trx.tax),
                    escape(trx.serviceCharge),
                    escape(trx.grandTotal),
                    escape(trx.paymentAmount),
                    escape(trx.changeAmount),
                    escape(trx.note ?: "-"),
                    escape(item.items.sumOf { it.item.qty }.toInt()),
                    escape(itemsSummary)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun generateProductsCsv(
        products: List<ProductWithCategory>,
        currencySymbol: String = "Rp"
    ): String {
        val sb = StringBuilder()
        sb.appendLine(
            listOf(
                "ID Produk",
                "Nama Produk",
                "Kategori",
                "Barcode",
                "Harga Jual ($currencySymbol)",
                "Harga Modal/HPP ($currencySymbol)",
                "Stok Saat Ini",
                "Satuan",
                "Tracking Stok",
                "Status Aktif"
            ).joinToString(",")
        )

        for (item in products) {
            val p = item.product
            sb.appendLine(
                listOf(
                    escape(p.id),
                    escape(p.name),
                    escape(item.category?.name ?: "Tanpa Kategori"),
                    escape(p.barcode ?: "-"),
                    escape(p.sellingPrice),
                    escape(p.costPrice),
                    escape(p.stock),
                    escape(p.unit),
                    escape(if (p.trackStock) "Aktif" else "Nonaktif"),
                    escape(if (p.isActive) "Aktif" else "Arsip")
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun generateExpensesCsv(
        expenses: List<ExpenseEntity>,
        currencySymbol: String = "Rp"
    ): String {
        val sb = StringBuilder()
        sb.appendLine(
            listOf(
                "ID Pengeluaran",
                "Tanggal",
                "Kategori Biaya",
                "Nominal ($currencySymbol)",
                "Catatan",
                "Dicatat Oleh"
            ).joinToString(",")
        )

        for (exp in expenses) {
            sb.appendLine(
                listOf(
                    escape(exp.id),
                    escape(dateFormat.format(Date(exp.date))),
                    escape(exp.category),
                    escape(exp.amount),
                    escape(exp.notes ?: "-"),
                    escape("-")
                ).joinToString(",")
            )
        }
        return sb.toString()
    }
}
