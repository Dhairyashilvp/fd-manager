package com.fdtracker.core.data.mapper

import com.fdtracker.core.data.db.entity.ReminderEntity
import com.fdtracker.core.domain.repository.Reminder

object ReminderMapper {
    fun toDomain(entity: ReminderEntity): Reminder {
        return Reminder(
            id = entity.id,
            fdAccountNumber = entity.fdAccountNumber,
            reminderType = entity.reminderType,
            triggerDate = entity.triggerDate,
            daysBefore = entity.daysBefore,
            isSent = entity.isSent,
            sentAt = entity.sentAt
        )
    }

    fun toEntity(domain: Reminder): ReminderEntity {
        return ReminderEntity(
            id = domain.id,
            fdAccountNumber = domain.fdAccountNumber,
            reminderType = domain.reminderType,
            triggerDate = domain.triggerDate,
            daysBefore = domain.daysBefore,
            isSent = domain.isSent,
            sentAt = domain.sentAt
        )
    }
}
