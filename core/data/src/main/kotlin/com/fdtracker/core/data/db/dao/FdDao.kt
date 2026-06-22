package com.fdtracker.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdtracker.core.data.db.entity.BankPrincipalTuple
import com.fdtracker.core.data.db.entity.FdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FdDao {
    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY maturityDate ASC")
    fun observeAllActive(): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE fdAccountNumber = :id")
    fun observeById(id: String): Flow<FdEntity?>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 AND maturityDate BETWEEN :from AND :to")
    fun observeByMaturityRange(from: Long, to: Long): Flow<List<FdEntity>>

    @Query("SELECT bankName, SUM(CAST(principalAmount AS REAL)) as total FROM fixed_deposits WHERE isActive = 1 GROUP BY bankName")
    fun observeBankWisePrincipal(): Flow<List<BankPrincipalTuple>>

    @Query("SELECT SUM(CAST(principalAmount AS REAL)) FROM fixed_deposits WHERE isActive = 1")
    fun observeTotalPrincipal(): Flow<Double?>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 AND bankName = :bankName ORDER BY maturityDate ASC")
    fun observeByBank(bankName: String): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY CAST(principalAmount AS REAL) DESC")
    fun observeAllActiveSortedByPrincipal(): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY CAST(interestRatePA AS REAL) DESC")
    fun observeAllActiveSortedByRate(): Flow<List<FdEntity>>

    @Query("SELECT createdAt FROM fixed_deposits WHERE fdAccountNumber = :fdAccountNumber LIMIT 1")
    suspend fun getCreatedAt(fdAccountNumber: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(fd: FdEntity)

    @Query("UPDATE fixed_deposits SET isActive = 0, updatedAt = :updatedAt WHERE fdAccountNumber = :fdAccountNumber")
    suspend fun softDelete(fdAccountNumber: String, updatedAt: Long = System.currentTimeMillis())
}
