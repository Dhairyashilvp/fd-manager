package com.fdtracker.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdtracker.core.data.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE fdAccountNumber = :fdAccountNumber ORDER BY triggerDate ASC")
    fun observeByFd(fdAccountNumber: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isSent = 0 ORDER BY triggerDate ASC")
    fun observeUnsent(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isSent = 0 AND triggerDate <= :currentTimeMillis ORDER BY triggerDate ASC")
    suspend fun getUnsentDue(currentTimeMillis: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET isSent = 1, sentAt = :sentAt WHERE id = :id")
    suspend fun markAsSent(id: Long, sentAt: Long)

    @Query("DELETE FROM reminders WHERE fdAccountNumber = :fdAccountNumber")
    suspend fun deleteByFd(fdAccountNumber: String)
}
