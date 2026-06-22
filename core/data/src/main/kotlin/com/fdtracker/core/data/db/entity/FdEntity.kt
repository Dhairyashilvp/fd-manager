package com.fdtracker.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fixed_deposits",
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["bankName"],
            childColumns = ["bankName"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("bankName"), Index("maturityDate")]
)
data class FdEntity(
    @PrimaryKey
    val fdAccountNumber: String,
    val bankName: String,
    val cifCustomerId: String,
    val primaryHolderName: String,
    val holdingMode: String,
    val jointHolderNames: String?,
    val principalAmount: String,
    val valueDate: Long,
    val maturityDate: Long,
    val tenureDays: Int,
    val interestRatePA: String,
    val compoundingFrequency: String,
    val estimatedMaturityAmount: String,
    val autoRenewalInstruction: String,
    val payoutAccountId: String,
    val gracePeriodDays: Int = 7,
    val nomineeName: String?,
    val interestPayoutFrequency: String?,
    val taxTdsApplicable: Boolean?,
    val taxExemptionForm: String?,
    val specialCategory: String?,
    val isTaxSaver: Boolean,
    val branchCode: String?,
    val ifscCode: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)
