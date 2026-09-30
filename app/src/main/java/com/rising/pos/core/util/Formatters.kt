package com.rising.pos.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptNumberGenerator {
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    fun generate(deviceId: String, sequenceNumber: Int, timestamp: Long = System.currentTimeMillis()): String {
        val dateStr = dateFormat.format(Date(timestamp))
        val paddedSeq = String.format(Locale.US, "%05d", sequenceNumber)
        val cleanDevice = deviceId.trim().ifEmpty { "A01" }
        return "TRX-$cleanDevice-$dateStr-$paddedSeq"
    }
}

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val decimalFormat = DecimalFormat("#,###", symbols)

    fun format(amount: Double, symbol: String = "Rp"): String {
        return "$symbol ${decimalFormat.format(amount)}"
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format(Locale.US, "%.1fM", amount / 1_000_000_000)
            amount >= 1_000_000 -> String.format(Locale.US, "%.1f jt", amount / 1_000_000)
            amount >= 1_000 -> String.format(Locale.US, "%.0fk", amount / 1_000)
            else -> amount.toInt().toString()
        }
    }
}

object ReceiptFormatter {
    fun formatReceiptText(
        trxDetails: com.rising.pos.core.database.entity.TransactionWithDetails,
        settings: com.rising.pos.core.datastore.BusinessSettings,
        paperWidth: Int = 32
    ): String {
        val trx = trxDetails.transaction
        val items = trxDetails.items
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(trx.createdAt))

        val sb = StringBuilder()
        fun center(text: String): String {
            if (text.length >= paperWidth) return text
            val pad = (paperWidth - text.length) / 2
            return " ".repeat(pad) + text
        }
        fun line(ch: Char = '-'): String = ch.toString().repeat(paperWidth)
        fun twoColumns(left: String, right: String): String {
            val space = paperWidth - left.length - right.length
            return if (space > 0) left + " ".repeat(space) + right else "$left $right"
        }

        sb.appendLine(center(settings.name.uppercase()))
        if (settings.address.isNotEmpty()) sb.appendLine(center(settings.address))
        if (settings.phone.isNotEmpty()) sb.appendLine(center("Telp: ${settings.phone}"))
        sb.appendLine(line('='))


        sb.appendLine(twoColumns("No. Struk:", trx.receiptNumber))
        sb.appendLine(twoColumns("Waktu:", dateStr))
        if (!trx.cashierId.isNullOrEmpty()) sb.appendLine(twoColumns("Kasir:", trx.cashierId))
        sb.appendLine(twoColumns("Tipe:", trx.orderType.name))
        sb.appendLine(twoColumns("Status:", trx.status.name))
        if (!trx.note.isNullOrEmpty()) {
            sb.appendLine("Catatan: ${trx.note}")
        }
        sb.appendLine(line('-'))

        for (itemDetail in items) {
            val item = itemDetail.item
            sb.appendLine(item.productName + if (!item.variantName.isNullOrEmpty()) " (${item.variantName})" else "")
            val left = "  ${item.qty.toInt()} x ${CurrencyFormatter.format(item.unitPrice, "")}"
            val right = CurrencyFormatter.format(item.subtotal, "")
            sb.appendLine(twoColumns(left, right))

            for (mod in itemDetail.modifiers) {
                val modLeft = "   + ${mod.modifierName}"
                val modRight = if (mod.price > 0) CurrencyFormatter.format(mod.price, "") else "Gratis"
                sb.appendLine(twoColumns(modLeft, modRight))
            }
        }

        sb.appendLine(line('-'))
        sb.appendLine(twoColumns("Subtotal:", CurrencyFormatter.format(trx.subtotal, settings.currencySymbol)))
        if (trx.discount > 0) {
            sb.appendLine(twoColumns("Diskon:", "-${CurrencyFormatter.format(trx.discount, settings.currencySymbol)}"))
        }
        if (trx.tax > 0) {
            sb.appendLine(twoColumns("Pajak:", CurrencyFormatter.format(trx.tax, settings.currencySymbol)))
        }
        if (trx.serviceCharge > 0) {
            sb.appendLine(twoColumns("Layanan:", CurrencyFormatter.format(trx.serviceCharge, settings.currencySymbol)))
        }
        sb.appendLine(line('='))
        sb.appendLine(twoColumns("TOTAL:", CurrencyFormatter.format(trx.grandTotal, settings.currencySymbol)))
        sb.appendLine(twoColumns("Metode Bayar:", trx.paymentMethod.name))
        sb.appendLine(twoColumns("Bayar:", CurrencyFormatter.format(trx.paymentAmount, settings.currencySymbol)))
        sb.appendLine(twoColumns("Kembalian:", CurrencyFormatter.format(trx.changeAmount, settings.currencySymbol)))

        sb.appendLine(line('-'))
        if (settings.footerNote.isNotEmpty()) {
            sb.appendLine(center(settings.footerNote))
        } else {
            sb.appendLine(center("Terima Kasih Atas Kunjungan Anda"))
        }

        return sb.toString()
    }
}

