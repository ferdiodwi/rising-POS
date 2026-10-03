package com.rising.pos.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Thin outline icons matching the cashier reference. */
object CashierIcons {
    private fun outline(name: String, draw: PathBuilder.() -> Unit) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.65f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()
    val Store = outline("Store") {
        moveTo(4f, 10f); lineTo(3f, 22f); lineTo(21f, 22f); lineTo(20f, 10f)
        moveTo(4f, 2f); lineTo(20f, 2f); lineTo(22f, 8f)
        curveTo(22f, 12f, 17f, 12f, 17f, 8f); lineTo(16f, 2f)
        moveTo(17f, 8f); curveTo(17f, 12f, 12f, 12f, 12f, 8f); lineTo(12f, 2f)
        moveTo(12f, 8f); curveTo(12f, 12f, 7f, 12f, 7f, 8f); lineTo(8f, 2f)
        moveTo(7f, 8f); curveTo(7f, 12f, 2f, 12f, 2f, 8f); lineTo(4f, 2f)
    }
    val Box = outline("Product box") {
        moveTo(12f, 2f); lineTo(21f, 7f); lineTo(21f, 17f); lineTo(12f, 22f); lineTo(3f, 17f); lineTo(3f, 7f); close()
        moveTo(3f, 7f); lineTo(12f, 12f); lineTo(21f, 7f); moveTo(12f, 12f); lineTo(12f, 22f)
    }
    val Receipt = outline("Receipt") {
        moveTo(5f, 2f); lineTo(19f, 2f); lineTo(19f, 22f); lineTo(16f, 20f); lineTo(13f, 22f); lineTo(10f, 20f); lineTo(7f, 22f); lineTo(5f, 21f); close()
        moveTo(8f, 6f); lineTo(16f, 6f); moveTo(8f, 10f); lineTo(16f, 10f); moveTo(8f, 14f); lineTo(12f, 14f)
    }
    val Barcode = outline("Barcode") {
        moveTo(6f, 2f); lineTo(2f, 2f); lineTo(2f, 6f); moveTo(18f, 2f); lineTo(22f, 2f); lineTo(22f, 6f)
        moveTo(2f, 18f); lineTo(2f, 22f); lineTo(6f, 22f); moveTo(18f, 22f); lineTo(22f, 22f); lineTo(22f, 18f)
        moveTo(6f, 7f); lineTo(6f, 17f); moveTo(9f, 5f); lineTo(9f, 19f); moveTo(12f, 7f); lineTo(12f, 17f)
        moveTo(15f, 5f); lineTo(15f, 19f); moveTo(18f, 7f); lineTo(18f, 17f)
    }
    val Person = outline("Customer") {
        moveTo(17f, 6f); curveTo(17f, 12.5f, 7f, 12.5f, 7f, 6f); curveTo(7f, -0.5f, 17f, -0.5f, 17f, 6f); close()
        moveTo(3f, 22f); lineTo(3f, 20f); curveTo(3f, 11f, 21f, 11f, 21f, 20f); lineTo(21f, 22f)
    }
    val Money = outline("Cash") {
        moveTo(2f, 4f); lineTo(22f, 4f); lineTo(22f, 20f); lineTo(2f, 20f); close()
        moveTo(15f, 12f); curveTo(15f, 17f, 9f, 17f, 9f, 12f); curveTo(9f, 7f, 15f, 7f, 15f, 12f); close()
        moveTo(2f, 9f); curveTo(5f, 9f, 6f, 7f, 6f, 4f); moveTo(18f, 4f); curveTo(18f, 7f, 20f, 9f, 22f, 9f)
        moveTo(2f, 15f); curveTo(5f, 15f, 6f, 17f, 6f, 20f); moveTo(18f, 20f); curveTo(18f, 17f, 20f, 15f, 22f, 15f)
    }
    val Bank = outline("Bank") {
        moveTo(2f, 7f); lineTo(12f, 2f); lineTo(22f, 7f); close()
        moveTo(3f, 9f); lineTo(21f, 9f); moveTo(5f, 9f); lineTo(5f, 19f); moveTo(10f, 9f); lineTo(10f, 19f)
        moveTo(14f, 9f); lineTo(14f, 19f); moveTo(19f, 9f); lineTo(19f, 19f)
        moveTo(3f, 19f); lineTo(21f, 19f); moveTo(2f, 22f); lineTo(22f, 22f)
    }
    val BarChart = outline("BarChart") {
        moveTo(3f, 20f); lineTo(21f, 20f)
        moveTo(7f, 20f); lineTo(7f, 13f)
        moveTo(12f, 20f); lineTo(12f, 8f)
        moveTo(17f, 20f); lineTo(17f, 4f)
    }
    val CashRegister = outline("CashRegister") {
        moveTo(4f, 8f); lineTo(20f, 8f); lineTo(20f, 19f); lineTo(4f, 19f); close()
        moveTo(7f, 4f); lineTo(17f, 4f); lineTo(17f, 8f); lineTo(7f, 8f); close()
        moveTo(11f, 13.5f); curveTo(11f, 14.3f, 11.4f, 15f, 12f, 15f); curveTo(12.6f, 15f, 13f, 14.3f, 13f, 13.5f); curveTo(13f, 12.7f, 12.6f, 12f, 12f, 12f); curveTo(11.4f, 12f, 11f, 12.7f, 11f, 13.5f); close()
    }
}
