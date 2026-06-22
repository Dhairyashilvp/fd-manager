package com.fdtracker.core.data.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("PRAGMA foreign_keys=OFF")

        migrateBanks(database)
        migrateFixedDeposits(database)
        ensureBanksExistForDeposits(database)
        migrateReminders(database)
        migrateSmtpConfig(database)

        database.execSQL("PRAGMA foreign_keys=ON")
    }
}

private fun migrateBanks(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `banks_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `bankName` TEXT NOT NULL,
            `bankCode` TEXT,
            `iconResName` TEXT,
            `colorHex` TEXT,
            `depositInsuranceLimitPaise` INTEGER NOT NULL
        )
        """.trimIndent()
    )

    if (database.tableExists("banks")) {
        database.execSQL(
            """
            INSERT OR IGNORE INTO `banks_new` (`id`, `bankName`, `bankCode`, `iconResName`, `colorHex`, `depositInsuranceLimitPaise`)
            SELECT
                ${database.columnOrDefault("banks", "id", "NULL")},
                ${database.columnOrDefault("banks", "bankName", "'UNKNOWN_BANK_' || rowid")},
                ${database.columnOrDefault("banks", "bankCode", "NULL")},
                ${database.columnOrDefault("banks", "iconResName", "NULL")},
                ${database.columnOrDefault("banks", "colorHex", "NULL")},
                ${database.columnOrDefault("banks", "depositInsuranceLimitPaise", "50000000")}
            FROM `banks`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `banks`")
    }

    database.execSQL("ALTER TABLE `banks_new` RENAME TO `banks`")
    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_banks_bankName` ON `banks` (`bankName`)")
}

private fun migrateFixedDeposits(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `fixed_deposits_new` (
            `fdAccountNumber` TEXT NOT NULL,
            `bankName` TEXT NOT NULL,
            `cifCustomerId` TEXT NOT NULL,
            `primaryHolderName` TEXT NOT NULL,
            `holdingMode` TEXT NOT NULL,
            `jointHolderNames` TEXT,
            `principalAmount` TEXT NOT NULL,
            `valueDate` INTEGER NOT NULL,
            `maturityDate` INTEGER NOT NULL,
            `tenureDays` INTEGER NOT NULL,
            `interestRatePA` TEXT NOT NULL,
            `compoundingFrequency` TEXT NOT NULL,
            `estimatedMaturityAmount` TEXT NOT NULL,
            `autoRenewalInstruction` TEXT NOT NULL,
            `payoutAccountId` TEXT NOT NULL,
            `gracePeriodDays` INTEGER NOT NULL,
            `nomineeName` TEXT,
            `interestPayoutFrequency` TEXT,
            `taxTdsApplicable` INTEGER,
            `taxExemptionForm` TEXT,
            `specialCategory` TEXT,
            `isTaxSaver` INTEGER NOT NULL,
            `branchCode` TEXT,
            `ifscCode` TEXT,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            `isActive` INTEGER NOT NULL,
            PRIMARY KEY(`fdAccountNumber`),
            FOREIGN KEY(`bankName`) REFERENCES `banks`(`bankName`) ON UPDATE NO ACTION ON DELETE RESTRICT
        )
        """.trimIndent()
    )

    if (database.tableExists("fixed_deposits")) {
        database.execSQL(
            """
            INSERT OR REPLACE INTO `fixed_deposits_new` (
                `fdAccountNumber`,
                `bankName`,
                `cifCustomerId`,
                `primaryHolderName`,
                `holdingMode`,
                `jointHolderNames`,
                `principalAmount`,
                `valueDate`,
                `maturityDate`,
                `tenureDays`,
                `interestRatePA`,
                `compoundingFrequency`,
                `estimatedMaturityAmount`,
                `autoRenewalInstruction`,
                `payoutAccountId`,
                `gracePeriodDays`,
                `nomineeName`,
                `interestPayoutFrequency`,
                `taxTdsApplicable`,
                `taxExemptionForm`,
                `specialCategory`,
                `isTaxSaver`,
                `branchCode`,
                `ifscCode`,
                `createdAt`,
                `updatedAt`,
                `isActive`
            )
            SELECT
                ${database.columnOrDefault("fixed_deposits", "fdAccountNumber", "'FD_' || rowid")},
                ${database.columnOrDefault("fixed_deposits", "bankName", "'UNKNOWN_BANK_' || rowid")},
                ${database.columnOrDefault("fixed_deposits", "cifCustomerId", "''")},
                ${database.columnOrDefault("fixed_deposits", "primaryHolderName", "''")},
                ${database.columnOrDefault("fixed_deposits", "holdingMode", "'SINGLE'")},
                ${database.columnOrDefault("fixed_deposits", "jointHolderNames", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "principalAmount", "'0'")},
                ${database.columnOrDefault("fixed_deposits", "valueDate", "0")},
                ${database.columnOrDefault("fixed_deposits", "maturityDate", "0")},
                ${database.columnOrDefault("fixed_deposits", "tenureDays", "0")},
                ${database.columnOrDefault("fixed_deposits", "interestRatePA", "'0'")},
                ${database.columnOrDefault("fixed_deposits", "compoundingFrequency", "'QUARTERLY'")},
                ${database.columnOrDefault("fixed_deposits", "estimatedMaturityAmount", "'0'")},
                ${database.columnOrDefault("fixed_deposits", "autoRenewalInstruction", "'PAYOUT'")},
                ${database.columnOrDefault("fixed_deposits", "payoutAccountId", "''")},
                ${database.columnOrDefault("fixed_deposits", "gracePeriodDays", "7")},
                ${database.columnOrDefault("fixed_deposits", "nomineeName", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "interestPayoutFrequency", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "taxTdsApplicable", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "taxExemptionForm", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "specialCategory", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "isTaxSaver", "0")},
                ${database.columnOrDefault("fixed_deposits", "branchCode", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "ifscCode", "NULL")},
                ${database.columnOrDefault("fixed_deposits", "createdAt", "CAST(strftime('%s','now') AS INTEGER) * 1000")},
                ${database.columnOrDefault("fixed_deposits", "updatedAt", "CAST(strftime('%s','now') AS INTEGER) * 1000")},
                ${database.columnOrDefault("fixed_deposits", "isActive", "1")}
            FROM `fixed_deposits`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `fixed_deposits`")
    }

    database.execSQL("ALTER TABLE `fixed_deposits_new` RENAME TO `fixed_deposits`")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_fixed_deposits_bankName` ON `fixed_deposits` (`bankName`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_fixed_deposits_maturityDate` ON `fixed_deposits` (`maturityDate`)")
}

private fun ensureBanksExistForDeposits(database: SupportSQLiteDatabase) {
    if (!database.tableExists("fixed_deposits") || !database.tableExists("banks")) return

    database.execSQL(
        """
        INSERT OR IGNORE INTO `banks` (`bankName`, `bankCode`, `iconResName`, `colorHex`, `depositInsuranceLimitPaise`)
        SELECT DISTINCT `bankName`, NULL, NULL, NULL, 50000000
        FROM `fixed_deposits`
        WHERE `bankName` IS NOT NULL AND TRIM(`bankName`) <> ''
        """.trimIndent()
    )
}

private fun migrateReminders(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `reminders_new` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `fdAccountNumber` TEXT NOT NULL,
            `reminderType` TEXT NOT NULL,
            `triggerDate` INTEGER NOT NULL,
            `daysBefore` INTEGER NOT NULL,
            `isSent` INTEGER NOT NULL,
            `sentAt` INTEGER,
            FOREIGN KEY(`fdAccountNumber`) REFERENCES `fixed_deposits`(`fdAccountNumber`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )

    if (database.tableExists("reminders")) {
        database.execSQL(
            """
            INSERT OR REPLACE INTO `reminders_new` (
                `id`,
                `fdAccountNumber`,
                `reminderType`,
                `triggerDate`,
                `daysBefore`,
                `isSent`,
                `sentAt`
            )
            SELECT
                ${database.columnOrDefault("reminders", "id", "NULL")},
                ${database.columnOrDefault("reminders", "fdAccountNumber", "''")},
                ${database.columnOrDefault("reminders", "reminderType", "'PUSH'")},
                ${database.columnOrDefault("reminders", "triggerDate", "0")},
                ${database.columnOrDefault("reminders", "daysBefore", "0")},
                ${database.columnOrDefault("reminders", "isSent", "0")},
                ${database.columnOrDefault("reminders", "sentAt", "NULL")}
            FROM `reminders`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `reminders`")
    }

    database.execSQL("ALTER TABLE `reminders_new` RENAME TO `reminders`")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_fdAccountNumber` ON `reminders` (`fdAccountNumber`)")
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_triggerDate` ON `reminders` (`triggerDate`)")
}

private fun migrateSmtpConfig(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `smtp_config_new` (
            `id` INTEGER NOT NULL,
            `host` TEXT NOT NULL,
            `port` INTEGER NOT NULL,
            `username` TEXT NOT NULL,
            `password` TEXT NOT NULL,
            `fromAddress` TEXT NOT NULL,
            `toAddress` TEXT NOT NULL,
            `useTls` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent()
    )

    if (database.tableExists("smtp_config")) {
        database.execSQL(
            """
            INSERT OR REPLACE INTO `smtp_config_new` (
                `id`,
                `host`,
                `port`,
                `username`,
                `password`,
                `fromAddress`,
                `toAddress`,
                `useTls`
            )
            SELECT
                ${database.columnOrDefault("smtp_config", "id", "1")},
                ${database.columnOrDefault("smtp_config", "host", "''")},
                ${database.columnOrDefault("smtp_config", "port", "587")},
                ${database.columnOrDefault("smtp_config", "username", "''")},
                ${database.columnOrDefault("smtp_config", "password", "''")},
                ${database.columnOrDefault("smtp_config", "fromAddress", "''")},
                ${database.columnOrDefault("smtp_config", "toAddress", "''")},
                ${database.columnOrDefault("smtp_config", "useTls", "1")}
            FROM `smtp_config`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `smtp_config`")
    }

    database.execSQL("ALTER TABLE `smtp_config_new` RENAME TO `smtp_config`")
}

private fun SupportSQLiteDatabase.tableExists(tableName: String): Boolean {
    val cursor = query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")
    return cursor.use { it.moveToFirst() }
}

private fun SupportSQLiteDatabase.columnExists(tableName: String, columnName: String): Boolean {
    val cursor = query("PRAGMA table_info(`$tableName`)")
    return cursor.use {
        val nameColumnIndex = it.getColumnIndex("name")
        while (it.moveToNext()) {
            if (it.getString(nameColumnIndex) == columnName) {
                return@use true
            }
        }
        false
    }
}

private fun SupportSQLiteDatabase.columnOrDefault(
    tableName: String,
    columnName: String,
    defaultExpression: String
): String {
    return if (columnExists(tableName, columnName)) {
        "`$columnName`"
    } else {
        defaultExpression
    }
}
