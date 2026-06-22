package com.fdtracker.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun BigDecimal.formatCurrency(): String {
    val formatted = this.setScale(2, RoundingMode.HALF_UP)
    return "₹${formatIndianNumber(formatted)}"
}

fun BigDecimal.formatPercent(): String {
    return "${this.setScale(2, RoundingMode.HALF_UP)}%"
}

private fun formatIndianNumber(value: BigDecimal): String {
    val parts = value.toPlainString().split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) ".${parts[1]}" else ".00"

    if (intPart.length <= 3) return "$intPart$decPart"

    val last3 = intPart.takeLast(3)
    val remaining = intPart.dropLast(3)
    val grouped = remaining.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3$decPart"
}

fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toLocalDate(): LocalDate {
    return java.time.Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun LocalDate.formatDisplay(): String {
    return this.format(DateTimeFormatter.ofPattern(Constants.DISPLAY_DATE_FORMAT))
}

fun LocalDate.daysBetween(other: LocalDate): Long {
    return ChronoUnit.DAYS.between(this, other)
}

fun LocalDate.isWithinDays(days: Int): Boolean {
    val today = LocalDate.now()
    return !this.isBefore(today) && !this.isAfter(today.plusDays(days.toLong()))
}

fun LocalDate.financialYear(): String {
    val fyStart = if (this.monthValue >= Constants.FY_START_MONTH) this.year else this.year - 1
    val fyEnd = fyStart + 1
    return "$fyStart-${fyEnd.toString().takeLast(2)}"
}

fun LocalDate.financialYearStart(): LocalDate {
    val year = if (this.monthValue >= Constants.FY_START_MONTH) this.year else this.year - 1
    return LocalDate.of(year, Constants.FY_START_MONTH, Constants.FY_START_DAY)
}

fun LocalDate.financialYearEnd(): LocalDate {
    val startYear = if (this.monthValue >= Constants.FY_START_MONTH) this.year else this.year - 1
    return LocalDate.of(startYear + 1, 3, 31)
}
