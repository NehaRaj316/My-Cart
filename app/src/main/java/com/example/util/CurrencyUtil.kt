package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {
    fun formatRupees(amount: Double): String {
        val rounded = amount.toLong()
        val format = NumberFormat.getIntegerInstance(Locale("en", "IN"))
        return "₹" + format.format(rounded)
    }

    fun formatRupeesWithDecimal(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        return "₹" + format.format(amount)
    }
}
