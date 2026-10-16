package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PersianNumberFormatter {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * Converts any string containing ASCII or Arabic digits to Persian digits.
     */
    fun formatDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            when {
                ch in '0'..'9' -> sb.append(PERSIAN_DIGITS[ch - '0'])
                ch in '٠'..'٩' -> sb.append(PERSIAN_DIGITS[ch - '٠'])
                ch == '.' || ch == '/' -> sb.append('٫') // Persian decimal separator
                ch == ',' -> sb.append('٬') // Persian thousands separator
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Normalizes a user input string (which may contain Persian or Arabic digits and Persian separators)
     * into a standard ASCII numeric string suitable for parsing into Double or Long.
     */
    fun normalizeToEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            when {
                ch in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                ch in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                ch in '0'..'9' -> sb.append(ch)
                ch == '٫' || ch == '.' || ch == '/' -> sb.append('.')
                ch == '٬' || ch == ',' || ch == ' ' || ch == '_' -> {
                    // ignore thousand separators or spaces
                }
                ch == '-' -> sb.append('-')
                else -> {
                    // Ignore other characters
                }
            }
        }
        return sb.toString()
    }

    /**
     * Safely parses a string with Persian or English digits to Double. Returns null if invalid or blank.
     */
    fun parseToDouble(input: String): Double? {
        val normalized = normalizeToEnglishDigits(input)
        if (normalized.isBlank()) return null
        return normalized.toDoubleOrNull()
    }

    /**
     * Safely parses a string with Persian or English digits to Long. Returns null if invalid or blank.
     */
    fun parseToLong(input: String): Long? {
        val normalized = normalizeToEnglishDigits(input)
        if (normalized.isBlank()) return null
        return normalized.toLongOrNull()
    }

    /**
     * Formats an integer or long with Persian digits and thousands separator '٬'.
     * Example: 15000000 -> "۱۵٬۰۰۰٬۰۰۰"
     */
    fun formatInteger(value: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = formatter.format(value)
        return formatDigits(formatted)
    }

    /**
     * Formats a double with Persian digits, thousand separator '٬' and Persian decimal separator '٫'.
     * Trims unnecessary trailing zeroes after decimal.
     * Example: 2.5 -> "۲٫۵"
     */
    fun formatDecimal(value: Double, maxDecimals: Int = 2): String {
        val pattern = if (maxDecimals == 0) "#,###" else "#,###." + "#".repeat(maxDecimals)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
            decimalSeparator = '٫'
        }
        val formatter = DecimalFormat(pattern, symbols)
        val formatted = formatter.format(value)
        return formatDigits(formatted)
    }

    /**
     * Formats area in square meters into both units display:
     * e.g. "۲٫۵ هکتار (۲۵٬۰۰۰ متر مربع)" or "۵۰۰ متر مربع" if less than 0.1 hectare.
     */
    fun formatAreaBoth(areaM2: Double?): String {
        if (areaM2 == null || areaM2 <= 0) return ""
        val ha = areaM2 / 10000.0
        val haFormatted = formatDecimal(ha, 2)
        val m2Formatted = formatDecimal(areaM2, 0)
        return "$haFormatted هکتار ($m2Formatted متر مربع)"
    }

    /**
     * Formats area in hectares with "هکتار" suffix.
     * e.g. "۲٫۵ هکتار"
     */
    fun formatAreaHectares(areaM2: Double?): String {
        if (areaM2 == null || areaM2 <= 0) return ""
        val ha = areaM2 / 10000.0
        return "${formatDecimal(ha, 2)} هکتار"
    }

    /**
     * Formats Tomans currency:
     * Full format in forms (decision 150): e.g. "۱۵٬۰۰۰٬۰۰۰ تومان"
     */
    fun formatCurrencyFull(tomans: Long): String {
        return "${formatInteger(tomans)} تومان"
    }

    /**
     * Short format in lists (decision 150):
     * >= 1,000,000,000 -> "X میلیارد"
     * >= 1,000,000 -> "X میلیون"
     * >= 1,000 -> "X هزار"
     * otherwise -> "X تومان"
     */
    fun formatCurrencyShort(tomans: Long): String {
        return when {
            tomans >= 1_000_000_000 -> {
                val value = tomans / 1_000_000_000.0
                "${formatDecimal(value, 1)} میلیارد"
            }
            tomans >= 1_000_000 -> {
                val value = tomans / 1_000_000.0
                "${formatDecimal(value, 1)} میلیون"
            }
            tomans >= 1_000 -> {
                val value = tomans / 1_000.0
                "${formatDecimal(value, 1)} هزار"
            }
            else -> {
                "${formatInteger(tomans)} تومان"
            }
        }
    }
}
