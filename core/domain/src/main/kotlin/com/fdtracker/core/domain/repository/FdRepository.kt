package com.fdtracker.core.domain.repository

import com.fdtracker.core.domain.model.FixedDeposit
import kotlinx.coroutines.flow.Flow

interface FdRepository {
    fun observeAllActive(): Flow<List<FixedDeposit>>
    fun observeById(id: String): Flow<FixedDeposit?>
    fun observeByMaturityRange(from: Long, to: Long): Flow<List<FixedDeposit>>
    fun observeByBank(bankName: String): Flow<List<FixedDeposit>>
    fun observeAllActiveSortedByPrincipal(): Flow<List<FixedDeposit>>
    fun observeAllActiveSortedByRate(): Flow<List<FixedDeposit>>
    fun observeBankWisePrincipal(): Flow<Map<String, Double>>
    fun observeTotalPrincipal(): Flow<Double?>
    suspend fun upsert(fd: FixedDeposit)
    suspend fun delete(fdAccountNumber: String)
}
