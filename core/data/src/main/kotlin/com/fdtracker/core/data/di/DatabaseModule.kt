package com.fdtracker.core.data.di

import android.content.Context
import androidx.room.Room
import com.fdtracker.core.common.Constants
import com.fdtracker.core.data.db.FdTrackerDatabase
import com.fdtracker.core.data.db.dao.BankDao
import com.fdtracker.core.data.db.dao.FdDao
import com.fdtracker.core.data.db.dao.ReminderDao
import com.fdtracker.core.data.db.dao.SmtpConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FdTrackerDatabase {
        return Room.databaseBuilder(
            context,
            FdTrackerDatabase::class.java,
            Constants.DATABASE_NAME
        ).addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideFdDao(database: FdTrackerDatabase): FdDao = database.fdDao()

    @Provides
    fun provideBankDao(database: FdTrackerDatabase): BankDao = database.bankDao()

    @Provides
    fun provideReminderDao(database: FdTrackerDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideSmtpConfigDao(database: FdTrackerDatabase): SmtpConfigDao = database.smtpConfigDao()
}
