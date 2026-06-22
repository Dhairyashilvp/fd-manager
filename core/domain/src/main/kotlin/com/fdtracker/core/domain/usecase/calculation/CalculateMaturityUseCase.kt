package com.fdtracker.core.domain.usecase.calculation

import com.fdtracker.core.domain.model.MaturityResult
import com.fdtracker.core.domain.model.PayoutFrequency
import com.fdtracker.core.domain.util.InterestCalculator
import java.math.BigDecimal
import java.time.LocalDate

class CalculateMaturityUseCase {
    operator fun invoke(
        principal: BigDecimal,
        annualRate: BigDecimal,
        tenureDays: Int,
        compounding: PayoutFrequency,
        valueDate: LocalDate
    ): MaturityResult {
        val maturityDate = InterestCalculator.calculateMaturityDate(valueDate, tenureDays)
        val maturityAmount = InterestCalculator.calculateMaturityAmount(
            principal, annualRate, tenureDays, compounding
        )
        val totalInterest = maturityAmount.subtract(principal)

        return MaturityResult(
            maturityDate = maturityDate,
            maturityAmount = maturityAmount,
            totalInterest = totalInterest
        )
    }
}
