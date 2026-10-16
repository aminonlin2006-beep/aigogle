package com.example

import com.example.util.PersianDateUtils
import com.example.util.PersianNumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PersianUtilsTest {

    @Test
    fun testPersianNumberFormatting() {
        val formatted = PersianNumberFormatter.formatDigits("1234567890")
        assertEquals("۱۲۳۴۵۶۷۸۹۰", formatted)

        val integerFormatted = PersianNumberFormatter.formatInteger(15000000L)
        assertEquals("۱۵٬۰۰۰٬۰۰۰", integerFormatted)

        val currencyFull = PersianNumberFormatter.formatCurrencyFull(15000000L)
        assertEquals("۱۵٬۰۰۰٬۰۰۰ تومان", currencyFull)

        val currencyShort = PersianNumberFormatter.formatCurrencyShort(15000000L)
        assertEquals("۱۵ میلیون", currencyShort)
    }

    @Test
    fun testAreaCalculations() {
        // 2.5 hectares = 25000 sq meters
        val areaText = PersianNumberFormatter.formatAreaBoth(25000.0)
        assertEquals("۲٫۵ هکتار (۲۵٬۰۰۰ متر مربع)", areaText)

        // Parse Persian digits to double
        val parsedDouble = PersianNumberFormatter.parseToDouble("۲٫۵")
        assertNotNull(parsedDouble)
        assertEquals(2.5, parsedDouble!!, 0.001)

        val parsedSqm = PersianNumberFormatter.parseToDouble("۲۵٬۰۰۰")
        assertNotNull(parsedSqm)
        assertEquals(25000.0, parsedSqm!!, 0.001)
    }

    @Test
    fun testJalaliDateConversion() {
        // Known date: 2026-10-16 is 1405-07-24 (24 Mehr 1405)
        val (jy, jm, jd) = PersianDateUtils.gregorianToJalali(2026, 10, 16)
        assertEquals(1405, jy)
        assertEquals(7, jm)
        assertEquals(24, jd)
    }
}
