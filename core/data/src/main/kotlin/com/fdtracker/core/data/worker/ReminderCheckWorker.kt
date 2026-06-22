package com.fdtracker.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fdtracker.core.common.Constants
import com.fdtracker.core.common.toLocalDate
import com.fdtracker.core.data.smtp.SmtpEmailService
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.repository.ReminderRepository
import com.fdtracker.core.domain.repository.UserPrefsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.math.BigDecimal

@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderRepository: ReminderRepository,
    private val fdRepository: FdRepository,
    private val smtpEmailService: SmtpEmailService,
    private val userPrefsRepository: UserPrefsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()

            val now = System.currentTimeMillis()
            val dueReminders = reminderRepository.getUnsentDue(now)

            val pushEnabled = userPrefsRepository.observePushRemindersEnabled().first()
            val emailEnabled = userPrefsRepository.observeEmailRemindersEnabled().first()

            for (reminder in dueReminders) {
                val fd = fdRepository.observeById(reminder.fdAccountNumber).first() ?: continue

                // Send push notification
                if (pushEnabled) {
                    sendPushNotification(
                        fdNumber = fd.fdAccountNumber,
                        bankName = fd.bankName,
                        daysBefore = reminder.daysBefore,
                        maturityAmount = fd.estimatedMaturityAmount
                    )
                }

                // Send email
                if (emailEnabled) {
                    smtpEmailService.sendMaturityReminder(
                        fdNumber = fd.fdAccountNumber,
                        bankName = fd.bankName,
                        maturityDate = fd.maturityDate,
                        maturityAmount = fd.estimatedMaturityAmount,
                        daysBefore = reminder.daysBefore
                    )
                }

                // Mark as sent
                reminderRepository.markAsSent(reminder.id, System.currentTimeMillis())
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for FD maturity reminders"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun sendPushNotification(
        fdNumber: String,
        bankName: String,
        daysBefore: Int,
        maturityAmount: BigDecimal
    ) {
        val title = when {
            daysBefore > 0 -> "$bankName FD maturing in $daysBefore days"
            daysBefore == 0 -> "$bankName FD matures today!"
            else -> "$bankName FD grace period ending"
        }
        val text = "FD #$fdNumber | Maturity: ₹${maturityAmount.toPlainString()}"

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(fdNumber.hashCode(), notification)
    }
}
