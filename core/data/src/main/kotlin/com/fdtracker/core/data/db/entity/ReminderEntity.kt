package com.fdtracker.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = FdEntity::class,
            parentColumns = ["fdAccountNumber"],
            childColumns = ["fdAccountNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fdAccountNumber"), Index("triggerDate")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fdAccountNumber: String,
    val reminderType: String,
    val triggerDate: Long,
    val daysBefore: Int,
    val isSent: Boolean,
    val sentAt: Long?
)
