package com.rising.pos.core.printer

import com.rising.pos.core.database.entity.TransactionWithDetails
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.util.CurrencyFormatter
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EscPosBuilder(
    private val charset: Charset = Charsets.ISO_8859_1
) {
    private val stream = ByteArrayOutputStream()

    init {
        // Initialize printer (ESC @)
        stream.write(byteArrayOf(0x1B, 0x40))
    }

    fun alignLeft(): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1B, 0x61, 0x00))
    }

    fun alignCenter(): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1B, 0x61, 0x01))
    }

    fun alignRight(): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1B, 0x61, 0x02))
    }

    fun bold(enable: Boolean): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1B, 0x45, if (enable) 0x01 else 0x00))
    }

    fun doubleHeight(enable: Boolean): EscPosBuilder = apply {
        // GS ! n (0x00 normal, 0x01 double height, 0x10 double width, 0x11 both)
        stream.write(byteArrayOf(0x1D, 0x21, if (enable) 0x01 else 0x00))
    }

    fun doubleWidthHeight(enable: Boolean): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1D, 0x21, if (enable) 0x11 else 0x00))
    }

    fun text(text: String): EscPosBuilder = apply {
        stream.write(text.toByteArray(charset))
    }

    fun textLine(line: String = ""): EscPosBuilder = apply {
        stream.write("$line\n".toByteArray(charset))
    }

    fun twoColumns(left: String, right: String, totalWidth: Int): EscPosBuilder = apply {
        val spaces = totalWidth - left.length - right.length
        val formatted = if (spaces > 0) {
            left + " ".repeat(spaces) + right
        } else {
            // If overflow, print left on one line and right right-aligned
            "$left\n" + " ".repeat((totalWidth - right.length).coerceAtLeast(0)) + right
        }
        textLine(formatted)
    }

    fun divider(char: Char = '-', totalWidth: Int): EscPosBuilder = apply {
        textLine(char.toString().repeat(totalWidth))
    }

    fun feed(lines: Int = 2): EscPosBuilder = apply {
        stream.write(byteArrayOf(0x1B, 0x64, lines.toByte()))
    }

    fun cutPaper(): EscPosBuilder = apply {
        // GS V 66 0 (Feed and cut)
        stream.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))
    }

    fun build(): ByteArray = stream.toByteArray()

    companion object {
        fun buildReceiptBytes(
            transactionWithDetails: TransactionWithDetails,
            settings: BusinessSettings
        ): ByteArray {
            val paperWidthChars = if (settings.printerPaperWidthMm >= 80) 48 else 32
            val trx = transactionWithDetails.transaction
            val items = transactionWithDetails.items
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormatter.format(Date(trx.createdAt))

            val builder = EscPosBuilder()

            // Header
            builder.alignCenter()
                .doubleWidthHeight(true)
                .bold(true)
                .textLine(settings.name.uppercase())
                .doubleWidthHeight(false)
                .bold(false)

            if (settings.address.isNotEmpty()) {
                builder.textLine(settings.address)
            }
            if (settings.phone.isNotEmpty()) {
                builder.textLine("Telp: ${settings.phone}")
            }

            builder.divider('=', paperWidthChars)

            // Info Section
            builder.alignLeft()
            builder.twoColumns("No. Struk:", trx.receiptNumber, paperWidthChars)
            builder.twoColumns("Waktu:", dateStr, paperWidthChars)
            if (!trx.cashierId.isNullOrEmpty()) {
                builder.twoColumns("Kasir:", trx.cashierId, paperWidthChars)
            }
            builder.twoColumns("Tipe:", trx.orderType.name, paperWidthChars)
            if (!trx.note.isNullOrEmpty()) {
                builder.textLine("Catatan: ${trx.note}")
            }

            builder.divider('-', paperWidthChars)

            // Items List
            for (itemDetail in items) {
                val item = itemDetail.item
                val itemTitle = item.productName + if (!item.variantName.isNullOrEmpty()) " (${item.variantName})" else ""
                builder.textLine(itemTitle)

                val leftCol = "  ${item.qty.toInt()} x ${CurrencyFormatter.format(item.unitPrice, "")}"
                val rightCol = CurrencyFormatter.format(item.subtotal, "")
                builder.twoColumns(leftCol, rightCol, paperWidthChars)

                for (mod in itemDetail.modifiers) {
                    val modLeft = "   + ${mod.modifierName}"
                    val modRight = if (mod.price > 0) CurrencyFormatter.format(mod.price, "") else "Gratis"
                    builder.twoColumns(modLeft, modRight, paperWidthChars)
                }
            }

            builder.divider('-', paperWidthChars)

            // Financial Summary
            builder.twoColumns("Subtotal:", CurrencyFormatter.format(trx.subtotal, settings.currencySymbol), paperWidthChars)
            if (trx.discount > 0) {
                builder.twoColumns("Diskon:", "-${CurrencyFormatter.format(trx.discount, settings.currencySymbol)}", paperWidthChars)
            }
            if (trx.tax > 0) {
                builder.twoColumns("Pajak:", CurrencyFormatter.format(trx.tax, settings.currencySymbol), paperWidthChars)
            }
            if (trx.serviceCharge > 0) {
                builder.twoColumns("Layanan:", CurrencyFormatter.format(trx.serviceCharge, settings.currencySymbol), paperWidthChars)
            }

            builder.divider('=', paperWidthChars)
            builder.bold(true)
            builder.twoColumns("TOTAL:", CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol), paperWidthChars)
            builder.bold(false)

            builder.twoColumns("Bayar (${trx.paymentMethod.name}):", CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol), paperWidthChars)
            builder.twoColumns("Kembalian:", CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol), paperWidthChars)

            builder.divider('-', paperWidthChars)

            // Footer
            builder.alignCenter()
            if (settings.footerNote.isNotEmpty()) {
                builder.textLine(settings.footerNote)
            } else {
                builder.textLine("Terima Kasih Atas Kunjungan Anda")
            }

            builder.feed(4)
            builder.cutPaper()

            return builder.build()
        }

        fun buildTestPrintBytes(
            storeName: String,
            paperWidthMm: Int
        ): ByteArray {
            val paperWidthChars = if (paperWidthMm >= 80) 48 else 32
            val builder = EscPosBuilder()

            builder.alignCenter()
                .doubleWidthHeight(true)
                .bold(true)
                .textLine(storeName.uppercase().ifEmpty { "POS THERMAL PRINTER" })
                .doubleWidthHeight(false)
                .bold(false)
                .divider('=', paperWidthChars)
                .textLine("UJI CETAK PRINTER BERHASIL")
                .textLine("Ukuran Kertas: $paperWidthMm mm ($paperWidthChars chars)")
                .textLine("Status: Siap Digunakan")
                .divider('-', paperWidthChars)
                .textLine("Rising POS - Offline First")
                .feed(4)
                .cutPaper()

            return builder.build()
        }
    }
}
