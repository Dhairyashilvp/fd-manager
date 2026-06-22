package com.fdtracker.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "banks",
    indices = [Index(value = ["bankName"], unique = true)]
)
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val bankCode: String?,
    val iconResName: String?,
    val colorHex: String?,
    val depositInsuranceLimitPaise: Long = 500_000_00L
)
