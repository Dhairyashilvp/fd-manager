package com.fdtracker.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smtp_config")
data class SmtpConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val toAddress: String,
    val useTls: Boolean
)
