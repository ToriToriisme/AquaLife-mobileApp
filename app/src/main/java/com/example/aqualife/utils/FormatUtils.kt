package com.example.aqualife.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatCurrency(amount: Int): String {
        return "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)} đ"
    }
    
    fun formatCurrency(amount: Double): String {
        return formatCurrency(amount.toInt())
    }
    
    fun parseCurrency(priceStr: String): Int {
        return priceStr.replace(".", "")
            .replace(" đ", "")
            .replace(",", "")
            .toIntOrNull() ?: 0
    }
}

