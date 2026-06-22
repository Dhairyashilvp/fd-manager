package com.fdtracker.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdtracker.core.data.db.entity.BankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT * FROM banks ORDER BY bankName ASC")
    fun observeAll(): Flow<List<BankEntity>>

    @Query("SELECT * FROM banks WHERE bankName = :bankName")
    fun observeByName(bankName: String): Flow<BankEntity?>

    @Query("SELECT * FROM banks WHERE bankName = :bankName LIMIT 1")
    suspend fun getByName(bankName: String): BankEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bank: BankEntity)

    @Query("DELETE FROM banks WHERE bankName = :bankName")
    suspend fun delete(bankName: String)
}
