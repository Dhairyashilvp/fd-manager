package com.fdtracker.core.data.repository

import com.fdtracker.core.data.db.dao.ReminderDao
import com.fdtracker.core.data.mapper.ReminderMapper
import com.fdtracker.core.domain.repository.Reminder
import com.fdtracker.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun observeByFd(fdAccountNumber: String): Flow<List<Reminder>> {
        return reminderDao.observeByFd(fdAccountNumber).map { entities ->
            entities.map { ReminderMapper.toDomain(it) }
        }
    }

    override fun observeUnsent(): Flow<List<Reminder>> {
        return reminderDao.observeUnsent().map { entities ->
            entities.map { ReminderMapper.toDomain(it) }
        }
    }

    override suspend fun getUnsentDue(currentTimeMillis: Long): List<Reminder> {
        return reminderDao.getUnsentDue(currentTimeMillis).map { ReminderMapper.toDomain(it) }
    }

    override suspend fun insert(reminder: Reminder) {
        reminderDao.insert(ReminderMapper.toEntity(reminder))
    }

    override suspend fun insertAll(reminders: List<Reminder>) {
        reminderDao.insertAll(reminders.map { ReminderMapper.toEntity(it) })
    }

    override suspend fun markAsSent(id: Long, sentAt: Long) {
        reminderDao.markAsSent(id, sentAt)
    }

    override suspend fun deleteByFd(fdAccountNumber: String) {
        reminderDao.deleteByFd(fdAccountNumber)
    }
}
