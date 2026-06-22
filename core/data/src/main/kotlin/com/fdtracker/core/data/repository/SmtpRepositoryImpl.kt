package com.fdtracker.core.data.repository

import com.fdtracker.core.data.db.dao.SmtpConfigDao
import com.fdtracker.core.data.db.entity.SmtpConfigEntity
import com.fdtracker.core.domain.repository.SmtpConfig
import com.fdtracker.core.domain.repository.SmtpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmtpRepositoryImpl @Inject constructor(
    private val smtpConfigDao: SmtpConfigDao
) : SmtpRepository {

    override fun observeConfig(): Flow<SmtpConfig?> {
        return smtpConfigDao.observeConfig().map { it?.toDomain() }
    }

    override suspend fun getConfig(): SmtpConfig? {
        return smtpConfigDao.getConfig()?.toDomain()
    }

    override suspend fun saveConfig(config: SmtpConfig) {
        smtpConfigDao.upsert(config.toEntity())
    }

    override suspend fun deleteConfig() {
        smtpConfigDao.delete()
    }

    private fun SmtpConfigEntity.toDomain() = SmtpConfig(
        id = id,
        host = host,
        port = port,
        username = username,
        password = password,
        fromAddress = fromAddress,
        toAddress = toAddress,
        useTls = useTls
    )

    private fun SmtpConfig.toEntity() = SmtpConfigEntity(
        id = id,
        host = host,
        port = port,
        username = username,
        password = password,
        fromAddress = fromAddress,
        toAddress = toAddress,
        useTls = useTls
    )
}
