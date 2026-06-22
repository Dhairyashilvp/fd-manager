package com.fdtracker.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdtracker.core.data.db.entity.SmtpConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmtpConfigDao {
    @Query("SELECT * FROM smtp_config WHERE id = 1")
    fun observeConfig(): Flow<SmtpConfigEntity?>

    @Query("SELECT * FROM smtp_config WHERE id = 1")
    suspend fun getConfig(): SmtpConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: SmtpConfigEntity)

    @Query("DELETE FROM smtp_config WHERE id = 1")
    suspend fun delete()
}
