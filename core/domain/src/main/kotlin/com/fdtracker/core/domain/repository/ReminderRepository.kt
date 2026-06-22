package com.fdtracker.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class Reminder(
    val id: Long = 0,
    val fdAccountNumber: String,
    val reminderType: String,
    val triggerDate: Long,
    val daysBefore: Int,
    val isSent: Boolean = false,
    val sentAt: Long? = null
)

interface ReminderRepository {
    fun observeByFd(fdAccountNumber: String): Flow<List<Reminder>>
    fun observeUnsent(): Flow<List<Reminder>>
    suspend fun getUnsentDue(currentTimeMillis: Long): List<Reminder>
    suspend fun insert(reminder: Reminder)
    suspend fun insertAll(reminders: List<Reminder>)
    suspend fun markAsSent(id: Long, sentAt: Long)
    suspend fun deleteByFd(fdAccountNumber: String)
}
