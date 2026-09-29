package com.example.util

import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    fun formatInr(amount: Double, includeDecimals: Boolean = false): String {
        val sign = if (amount < 0) "-" else ""
        val absAmount = abs(amount)
        val longVal = absAmount.toLong()
        val decimals = if (includeDecimals) {
            val dec = ((absAmount - longVal) * 100).toLong()
            if (dec > 0) ".%02d".format(Locale.US, dec) else ""
        } else ""

        val formattedInt = formatIndianGrouping(longVal)
        return "$sign₹$formattedInt$decimals"
    }

    fun formatInrSigned(amount: Double, type: String): String {
        val formatted = formatInr(amount, includeDecimals = false)
        return if (type == "earned") "+$formatted" else "-$formatted"
    }

    fun formatCompactInr(amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        val absAmount = abs(amount)
        return when {
            absAmount >= 10000000.0 -> {
                val cr = absAmount / 10000000.0
                val df = DecimalFormat("0.00")
                "$sign₹${df.format(cr)} Cr"
            }
            absAmount >= 100000.0 -> {
                val l = absAmount / 100000.0
                val df = DecimalFormat("0.00")
                "$sign₹${df.format(l)} L"
            }
            absAmount >= 1000.0 -> {
                val k = absAmount / 1000.0
                val df = DecimalFormat("#.#")
                "$sign₹${df.format(k)} k"
            }
            else -> "$sign₹${absAmount.toLong()}"
        }
    }

    private fun formatIndianGrouping(value: Long): String {
        val s = value.toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        val rest = s.substring(0, s.length - 3)
        val sb = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }
        return "${sb.reverse()},$lastThree"
    }
}
