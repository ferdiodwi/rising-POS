package com.rising.pos.ui

import com.rising.pos.feature.pos.components.cashKeyInput
import org.junit.Assert.assertEquals
import org.junit.Test

class CashKeyInputTest {
    @Test fun aPresetIsReplacedByTheFirstDigitThenThousandsAppend() {
        val first = cashKeyInput("50000", "2", true)
        assertEquals("2", first)
        assertEquals("2000", cashKeyInput(first, "000", false))
    }
    @Test fun deleteRemovesOneDigitAndNeverLeavesAnEmptyAmount() {
        assertEquals("5000", cashKeyInput("50000", "delete", true))
        assertEquals("0", cashKeyInput("1", "delete", false))
        assertEquals("0", cashKeyInput("0", "000", false))
    }
    @Test fun oversizedOrUnknownInputCannotOverflowMoney() {
        assertEquals("999999999999", cashKeyInput("999999999999", "9", false))
        assertEquals("1000", cashKeyInput("1000", "-", false))
    }
}
