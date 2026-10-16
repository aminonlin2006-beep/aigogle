package com.example.util

import java.util.Calendar
import java.util.TimeZone

/**
 * Jalali (Solar Hijri) calendar utilities without external dependencies.
 * Implements astronomical algorithm for Gregorian <-> Jalali conversions.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeekName: String = "",
    val monthName: String = ""
) {
    /**
     * Formats as e.g. "۱۴۰۵/۰۷/۲۴"
     */
    fun toNumericString(): String {
        val y = PersianNumberFormatter.formatDigits(year.toString())
        val m = PersianNumberFormatter.formatDigits(month.toString().padStart(2, '0'))
        val d = PersianNumberFormatter.formatDigits(day.toString().padStart(2, '0'))
        return "$y/$m/$d"
    }

    /**
     * Formats as e.g. "جمعه ۲۴ مهر ۱۴۰۵"
     */
    fun toLongFormat(): String {
        val d = PersianNumberFormatter.formatDigits(day.toString())
        val y = PersianNumberFormatter.formatDigits(year.toString())
        return "$dayOfWeekName $d $monthName $y"
    }

    /**
     * Formats for backup filename with Persian digits: e.g. "۱۴۰۵-۰۷-۲۴"
     */
    fun toBackupDateString(): String {
        val y = PersianNumberFormatter.formatDigits(year.toString())
        val m = PersianNumberFormatter.formatDigits(month.toString().padStart(2, '0'))
        val d = PersianNumberFormatter.formatDigits(day.toString().padStart(2, '0'))
        return "$y-$m-$d"
    }

    /**
     * Formats standard ISO-like key e.g. "1405-07-24" in English for internal storage
     */
    fun toKey(): String {
        return "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }
}

object PersianDateUtils {

    private val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val DAY_OF_WEEK_NAMES = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    /**
     * Converts a timestamp in milliseconds to JalaliDate.
     */
    fun fromTimestamp(epochMillis: Long): JalaliDate {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply {
            timeInMillis = epochMillis
        }
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val calDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 7=Saturday
        val dayOfWeekName = DAY_OF_WEEK_NAMES[calDayOfWeek - 1]

        val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
        val monthName = MONTH_NAMES.getOrElse(jMonth - 1) { "" }

        return JalaliDate(
            year = jYear,
            month = jMonth,
            day = jDay,
            dayOfWeekName = dayOfWeekName,
            monthName = monthName
        )
    }

    /**
     * Gets today's Jalali date.
     */
    fun today(): JalaliDate {
        return fromTimestamp(System.currentTimeMillis())
    }

    /**
     * Formats timestamp into long Persian date and time: e.g. "۲۴ مهر ۱۴۰۵ - ۱۲:۳۰"
     */
    fun formatDateTime(epochMillis: Long): String {
        val jDate = fromTimestamp(epochMillis)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply {
            timeInMillis = epochMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        val timeStr = PersianNumberFormatter.formatDigits("$hour:$minute")
        val d = PersianNumberFormatter.formatDigits(jDate.day.toString())
        val y = PersianNumberFormatter.formatDigits(jDate.year.toString())
        return "$d ${jDate.monthName} $y - $timeStr"
    }

    /**
     * Algorithmic conversion from Gregorian to Jalali
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) (gy + 1) else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        if (gm > 2 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            days++
        }
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }
}
