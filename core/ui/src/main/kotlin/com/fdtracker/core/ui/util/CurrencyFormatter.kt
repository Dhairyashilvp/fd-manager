package com.fdtracker.core.ui.util

import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyFormatter {
    fun format(amount: BigDecimal): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)
        val parts = scaled.toPlainString().split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) ".${parts[1]}" else ".00"

        if (intPart.length <= 3) return "₹$intPart$decPart"

        val last3 = intPart.takeLast(3)
        val remaining = intPart.dropLast(3)
        val grouped = remaining.reversed().chunked(2).joinToString(",").reversed()
        return "₹$grouped,$last3$decPart"
    }

    fun formatCompact(amount: BigDecimal): String {
        return when {
            amount >= BigDecimal("10000000") -> "₹${amount.divide(BigDecimal("10000000"), 2, RoundingMode.HALF_UP)}Cr"
            amount >= BigDecimal("100000") -> "₹${amount.divide(BigDecimal("100000"), 2, RoundingMode.HALF_UP)}L"
            amount >= BigDecimal("1000") -> "₹${amount.divide(BigDecimal("1000"), 1, RoundingMode.HALF_UP)}K"
            else -> format(amount)
        }
    }
}
