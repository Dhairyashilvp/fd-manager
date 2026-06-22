package com.fdtracker.core.domain.usecase.calculation

import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.util.InterestCalculator
import java.math.BigDecimal
import java.time.LocalDate

class CalculateAccruedInterestUseCase {
    operator fun invoke(fd: FixedDeposit, currentDate: LocalDate = LocalDate.now()): BigDecimal {
        return InterestCalculator.calculateAccruedInterest(
            principal = fd.principalAmount,
            annualRate = fd.interestRatePA,
            valueDate = fd.valueDate,
            currentDate = if (currentDate.isAfter(fd.maturityDate)) fd.maturityDate else currentDate,
            compounding = fd.compoundingFrequency
        )
    }
}
