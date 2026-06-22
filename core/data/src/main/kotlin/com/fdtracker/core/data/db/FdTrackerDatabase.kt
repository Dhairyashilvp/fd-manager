package com.fdtracker.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fdtracker.core.data.db.converter.Converters
import com.fdtracker.core.data.db.dao.BankDao
import com.fdtracker.core.data.db.dao.FdDao
import com.fdtracker.core.data.db.dao.ReminderDao
import com.fdtracker.core.data.db.dao.SmtpConfigDao
import com.fdtracker.core.data.db.entity.BankEntity
import com.fdtracker.core.data.db.entity.FdEntity
import com.fdtracker.core.data.db.entity.ReminderEntity
import com.fdtracker.core.data.db.entity.SmtpConfigEntity

@Database(
    entities = [
        FdEntity::class,
        BankEntity::class,
        ReminderEntity::class,
        SmtpConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FdTrackerDatabase : RoomDatabase() {
    abstract fun fdDao(): FdDao
    abstract fun bankDao(): BankDao
    abstract fun reminderDao(): ReminderDao
    abstract fun smtpConfigDao(): SmtpConfigDao
}
