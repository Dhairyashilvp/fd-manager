package com.fdtracker.core.data.mapper

import com.fdtracker.core.common.toEpochMillis
import com.fdtracker.core.common.toLocalDate
import com.fdtracker.core.data.db.entity.FdEntity
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.model.HoldingMode
import com.fdtracker.core.domain.model.PayoutFrequency
import com.fdtracker.core.domain.model.RenewalInstruction
import com.fdtracker.core.domain.model.SpecialCategory
import com.fdtracker.core.domain.model.TaxExemptionForm
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

object FdMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: FdEntity): FixedDeposit {
        return FixedDeposit(
            fdAccountNumber = entity.fdAccountNumber,
            bankName = entity.bankName,
            cifCustomerId = entity.cifCustomerId,
            primaryHolderName = entity.primaryHolderName,
            holdingMode = enumValueOfSafe(entity.holdingMode, HoldingMode.SINGLE),
            jointHolderNames = entity.jointHolderNames?.let {
                try { json.decodeFromString<List<String>>(it) } catch (_: Exception) { emptyList() }
            } ?: emptyList(),
            principalAmount = BigDecimal(entity.principalAmount),
            valueDate = entity.valueDate.toLocalDate(),
            maturityDate = entity.maturityDate.toLocalDate(),
            tenureDays = entity.tenureDays,
            interestRatePA = BigDecimal(entity.interestRatePA),
            compoundingFrequency = enumValueOfSafe(entity.compoundingFrequency, PayoutFrequency.QUARTERLY),
            estimatedMaturityAmount = BigDecimal(entity.estimatedMaturityAmount),
            autoRenewalInstruction = enumValueOfSafe(entity.autoRenewalInstruction, RenewalInstruction.PAYOUT),
            payoutAccountId = entity.payoutAccountId,
            gracePeriodDays = entity.gracePeriodDays,
            nomineeName = entity.nomineeName,
            interestPayoutFrequency = entity.interestPayoutFrequency?.let {
                enumValueOfSafe(it, PayoutFrequency.QUARTERLY)
            },
            taxTdsApplicable = entity.taxTdsApplicable,
            taxExemptionForm = entity.taxExemptionForm?.let {
                enumValueOfSafe(it, TaxExemptionForm.NONE)
            } ?: TaxExemptionForm.NONE,
            specialCategory = entity.specialCategory?.let {
                enumValueOfSafe(it, SpecialCategory.STANDARD)
            } ?: SpecialCategory.STANDARD,
            isTaxSaver = entity.isTaxSaver,
            branchCode = entity.branchCode,
            ifscCode = entity.ifscCode,
            isActive = entity.isActive
        )
    }

    fun toEntity(domain: FixedDeposit, existingCreatedAt: Long? = null): FdEntity {
        val now = System.currentTimeMillis()
        return FdEntity(
            fdAccountNumber = domain.fdAccountNumber,
            bankName = domain.bankName,
            cifCustomerId = domain.cifCustomerId,
            primaryHolderName = domain.primaryHolderName,
            holdingMode = domain.holdingMode.name,
            jointHolderNames = if (domain.jointHolderNames.isNotEmpty()) {
                json.encodeToString(domain.jointHolderNames)
            } else null,
            principalAmount = domain.principalAmount.toPlainString(),
            valueDate = domain.valueDate.toEpochMillis(),
            maturityDate = domain.maturityDate.toEpochMillis(),
            tenureDays = domain.tenureDays,
            interestRatePA = domain.interestRatePA.toPlainString(),
            compoundingFrequency = domain.compoundingFrequency.name,
            estimatedMaturityAmount = domain.estimatedMaturityAmount.toPlainString(),
            autoRenewalInstruction = domain.autoRenewalInstruction.name,
            payoutAccountId = domain.payoutAccountId,
            gracePeriodDays = domain.gracePeriodDays,
            nomineeName = domain.nomineeName,
            interestPayoutFrequency = domain.interestPayoutFrequency?.name,
            taxTdsApplicable = domain.taxTdsApplicable,
            taxExemptionForm = domain.taxExemptionForm.name,
            specialCategory = domain.specialCategory.name,
            isTaxSaver = domain.isTaxSaver,
            branchCode = domain.branchCode,
            ifscCode = domain.ifscCode,
            createdAt = existingCreatedAt ?: now,
            updatedAt = now,
            isActive = domain.isActive
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOfSafe(name: String, default: T): T {
        return try {
            enumValueOf<T>(name)
        } catch (_: IllegalArgumentException) {
            default
        }
    }
}
