package com.fdtracker.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class SmtpConfig(
    val id: Int = 1,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val toAddress: String,
    val useTls: Boolean = true
)

interface SmtpRepository {
    fun observeConfig(): Flow<SmtpConfig?>
    suspend fun getConfig(): SmtpConfig?
    suspend fun saveConfig(config: SmtpConfig)
    suspend fun deleteConfig()
}
