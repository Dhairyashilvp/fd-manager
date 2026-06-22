package com.fdtracker.core.data.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fdtracker.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun initialize() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderWork = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            Constants.REMINDER_WORK_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }
}
