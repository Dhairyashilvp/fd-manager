package com.fdtracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

data class BankInfo(
    val id: Long = 0,
    val bankName: String,
    val bankCode: String? = null,
    val iconResName: String? = null,
    val colorHex: String? = null,
    val depositInsuranceLimit: BigDecimal = BigDecimal("500000")
)

interface BankRepository {
    fun observeAll(): Flow<List<BankInfo>>
    fun observeByName(bankName: String): Flow<BankInfo?>
    suspend fun upsert(bank: BankInfo)
    suspend fun delete(bankName: String)
    suspend fun getInsuranceLimit(bankName: String): BigDecimal
}
