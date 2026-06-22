package com.fdtracker.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class FixedDeposit(
    val fdAccountNumber: String,
    val bankName: String,
    val cifCustomerId: String,
    val primaryHolderName: String,
    val holdingMode: HoldingMode,
    val jointHolderNames: List<String>,
    val principalAmount: BigDecimal,
    val valueDate: LocalDate,
    val maturityDate: LocalDate,
    val tenureDays: Int,
    val interestRatePA: BigDecimal,
    val compoundingFrequency: PayoutFrequency,
    val estimatedMaturityAmount: BigDecimal,
    val autoRenewalInstruction: RenewalInstruction,
    val payoutAccountId: String,
    val gracePeriodDays: Int = 7,
    val nomineeName: String? = null,
    val interestPayoutFrequency: PayoutFrequency? = null,
    val taxTdsApplicable: Boolean? = null,
    val taxExemptionForm: TaxExemptionForm = TaxExemptionForm.NONE,
    val specialCategory: SpecialCategory = SpecialCategory.STANDARD,
    val isTaxSaver: Boolean = false,
    val branchCode: String? = null,
    val ifscCode: String? = null,
    val isActive: Boolean = true
)

enum class HoldingMode {
    SINGLE, JOINTLY, EITHER_OR_SURVIVOR, FORMER_OR_SURVIVOR
}

enum class RenewalInstruction {
    RENEW_BOTH, RENEW_PRINCIPAL, PAYOUT
}

enum class PayoutFrequency {
    MONTHLY, QUARTERLY, HALF_YEARLY, ANNUALLY, CUMULATIVE;

    fun periodsPerYear(): Int = when (this) {
        MONTHLY -> 12
        QUARTERLY -> 4
        HALF_YEARLY -> 2
        ANNUALLY -> 1
        CUMULATIVE -> 4 // Default quarterly compounding for cumulative
    }
}

enum class SpecialCategory {
    STANDARD, SENIOR_CITIZEN, STAFF
}

enum class TaxExemptionForm {
    NONE, FORM_15G, FORM_15H
}
