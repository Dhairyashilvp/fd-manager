package com.fdtracker.core.ui.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateFormatter {
    private val displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val monthYearFormat = DateTimeFormatter.ofPattern("MMM yyyy")
    private val shortFormat = DateTimeFormatter.ofPattern("dd/MM/yy")

    fun formatDisplay(date: LocalDate): String = date.format(displayFormat)

    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.format(monthYearFormat)

    fun formatShort(date: LocalDate): String = date.format(shortFormat)

    fun daysUntil(date: LocalDate): Long = ChronoUnit.DAYS.between(LocalDate.now(), date)

    fun formatRelative(date: LocalDate): String {
        val days = daysUntil(date)
        return when {
            days < 0 -> "${-days} days ago"
            days == 0L -> "Today"
            days == 1L -> "Tomorrow"
            days <= 30 -> "$days days"
            days <= 365 -> "${days / 30} months"
            else -> "${days / 365} years"
        }
    }
}
