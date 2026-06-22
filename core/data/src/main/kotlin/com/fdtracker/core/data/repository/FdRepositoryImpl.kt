package com.fdtracker.core.data.repository

import com.fdtracker.core.data.db.dao.FdDao
import com.fdtracker.core.data.mapper.FdMapper
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FdRepositoryImpl @Inject constructor(
    private val fdDao: FdDao
) : FdRepository {

    override fun observeAllActive(): Flow<List<FixedDeposit>> {
        return fdDao.observeAllActive().map { entities ->
            entities.map { FdMapper.toDomain(it) }
        }
    }

    override fun observeById(id: String): Flow<FixedDeposit?> {
        return fdDao.observeById(id).map { it?.let { entity -> FdMapper.toDomain(entity) } }
    }

    override fun observeByMaturityRange(from: Long, to: Long): Flow<List<FixedDeposit>> {
        return fdDao.observeByMaturityRange(from, to).map { entities ->
            entities.map { FdMapper.toDomain(it) }
        }
    }

    override fun observeByBank(bankName: String): Flow<List<FixedDeposit>> {
        return fdDao.observeByBank(bankName).map { entities ->
            entities.map { FdMapper.toDomain(it) }
        }
    }

    override fun observeAllActiveSortedByPrincipal(): Flow<List<FixedDeposit>> {
        return fdDao.observeAllActiveSortedByPrincipal().map { entities ->
            entities.map { FdMapper.toDomain(it) }
        }
    }

    override fun observeAllActiveSortedByRate(): Flow<List<FixedDeposit>> {
        return fdDao.observeAllActiveSortedByRate().map { entities ->
            entities.map { FdMapper.toDomain(it) }
        }
    }

    override fun observeBankWisePrincipal(): Flow<Map<String, Double>> {
        return fdDao.observeBankWisePrincipal().map { tuples ->
            tuples.associate { it.bankName to it.total }
        }
    }

    override fun observeTotalPrincipal(): Flow<Double?> {
        return fdDao.observeTotalPrincipal()
    }

    override suspend fun upsert(fd: FixedDeposit) {
        val existingCreatedAt = fdDao.getCreatedAt(fd.fdAccountNumber)
        fdDao.upsert(FdMapper.toEntity(fd, existingCreatedAt))
    }

    override suspend fun delete(fdAccountNumber: String) {
        fdDao.softDelete(fdAccountNumber)
    }
}
