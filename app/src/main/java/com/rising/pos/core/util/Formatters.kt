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
    private val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
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
