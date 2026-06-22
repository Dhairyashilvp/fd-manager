package com.fdtracker.core.data.repository

import com.fdtracker.core.common.Constants
import com.fdtracker.core.data.db.dao.BankDao
import com.fdtracker.core.data.db.entity.BankEntity
import com.fdtracker.core.domain.repository.BankInfo
import com.fdtracker.core.domain.repository.BankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankRepositoryImpl @Inject constructor(
    private val bankDao: BankDao
) : BankRepository {

    override fun observeAll(): Flow<List<BankInfo>> {
        return bankDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeByName(bankName: String): Flow<BankInfo?> {
        return bankDao.observeByName(bankName).map { it?.toDomain() }
    }

    override suspend fun upsert(bank: BankInfo) {
        bankDao.insert(
            BankEntity(
                id = bank.id,
                bankName = bank.bankName,
                bankCode = bank.bankCode,
                iconResName = bank.iconResName,
                colorHex = bank.colorHex,
                depositInsuranceLimitPaise = bank.depositInsuranceLimit
                    .multiply(BigDecimal(100)).toLong()
            )
        )
    }

    override suspend fun delete(bankName: String) {
        bankDao.delete(bankName)
    }

    override suspend fun getInsuranceLimit(bankName: String): BigDecimal {
        val bank = bankDao.getByName(bankName)
        return bank?.let {
            BigDecimal(it.depositInsuranceLimitPaise).divide(BigDecimal(100))
        } ?: Constants.DICGC_INSURANCE_LIMIT
    }

    private fun BankEntity.toDomain() = BankInfo(
        id = id,
        bankName = bankName,
        bankCode = bankCode,
        iconResName = iconResName,
        colorHex = colorHex,
        depositInsuranceLimit = BigDecimal(depositInsuranceLimitPaise).divide(BigDecimal(100))
    )
}
