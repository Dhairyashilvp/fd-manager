package com.fdtracker.core.domain.usecase.reminder

import com.fdtracker.core.common.Constants
import com.fdtracker.core.common.Result
import com.fdtracker.core.common.toEpochMillis
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.Reminder
import com.fdtracker.core.domain.repository.ReminderRepository
import java.time.LocalDate

class ScheduleRemindersUseCase(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(fd: FixedDeposit): Result<Unit> {
        return Result.runCatching {
            // Delete existing reminders for this FD
            reminderRepository.deleteByFd(fd.fdAccountNumber)

            val reminders = mutableListOf<Reminder>()
            val maturityDate = fd.maturityDate

            // T-14, T-7, T-1 days before maturity
            for (daysBefore in Constants.REMINDER_DAYS_BEFORE) {
                val triggerDate = maturityDate.minusDays(daysBefore.toLong())
                if (triggerDate.isAfter(LocalDate.now()) || triggerDate.isEqual(LocalDate.now())) {
                    reminders.add(
                        Reminder(
                            fdAccountNumber = fd.fdAccountNumber,
                            reminderType = "PUSH",
                            triggerDate = triggerDate.toEpochMillis(),
                            daysBefore = daysBefore
                        )
                    )
                }
            }

            // T+grace end reminder
            val graceEndDate = maturityDate.plusDays(fd.gracePeriodDays.toLong())
            val graceEndDaysBefore = -(fd.gracePeriodDays)
            reminders.add(
                Reminder(
                    fdAccountNumber = fd.fdAccountNumber,
                    reminderType = "PUSH",
                    triggerDate = graceEndDate.toEpochMillis(),
                    daysBefore = graceEndDaysBefore
                )
            )

            reminderRepository.insertAll(reminders)
        }
    }
}
