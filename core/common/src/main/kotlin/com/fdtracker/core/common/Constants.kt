package com.fdtracker.core.common

import java.math.BigDecimal

object Constants {
    const val DATABASE_NAME = "fd_tracker_db"
    const val DATABASE_VERSION = 1

    // DICGC deposit insurance limit per depositor per bank (₹5,00,000 = 500000)
    val DICGC_INSURANCE_LIMIT: BigDecimal = BigDecimal("500000")
    const val DICGC_INSURANCE_LIMIT_PAISE: Long = 500_000_00L

    // TDS threshold for interest income per bank per FY
    val TDS_THRESHOLD_GENERAL: BigDecimal = BigDecimal("40000")
    val TDS_THRESHOLD_SENIOR_CITIZEN: BigDecimal = BigDecimal("50000")

    // Reminder days before maturity
    val REMINDER_DAYS_BEFORE = listOf(14, 7, 1, 0)

    // Grace period default
    const val DEFAULT_GRACE_PERIOD_DAYS = 7

    // Premature withdrawal penalty rate (typical 1%)
    val DEFAULT_PENALTY_RATE: BigDecimal = BigDecimal("1.0")

    // WorkManager
    const val REMINDER_WORK_NAME = "fd_reminder_check"
    const val REMINDER_WORK_INTERVAL_HOURS = 6L

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "fd_maturity_reminders"
    const val NOTIFICATION_CHANNEL_NAME = "Maturity Reminders"

    // DataStore
    const val DATASTORE_NAME = "fd_tracker_preferences"

    // Date formats
    const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    const val ISO_DATE_FORMAT = "yyyy-MM-dd"

    // Senior citizen age threshold
    const val SENIOR_CITIZEN_AGE = 60

    // Financial Year start month (April = 4)
    const val FY_START_MONTH = 4
    const val FY_START_DAY = 1
}
