package com.fdtracker.core.domain.usecase.calculation

import com.fdtracker.core.common.Constants
import com.fdtracker.core.domain.model.BreakFdRecommendation
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.util.InterestCalculator
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CalculateBreakFdPenaltyUseCase(
    private val fdRepository: FdRepository
) {
    suspend operator fun invoke(targetAmount: BigDecimal): List<BreakFdRecommendation> {
        val activeFds = fdRepository.observeAllActive().first()
        val today = LocalDate.now()

        // Filter out tax saver FDs (cannot be broken prematurely)
        val breakableFds = activeFds.filter { !it.isTaxSaver && it.maturityDate.isAfter(today) }

        return breakableFds.map { fd ->
            val elapsedDays = ChronoUnit.DAYS.between(fd.valueDate, today).toInt()
            val penaltyRate = Constants.DEFAULT_PENALTY_RATE
            val applicableRate = fd.interestRatePA
            val effectiveRate = applicableRate.subtract(penaltyRate)

            val payoutAmount = InterestCalculator.calculatePrematureWithdrawal(
                principal = fd.principalAmount,
                applicableRate = applicableRate,
                penaltyRate = penaltyRate,
                elapsedDays = elapsedDays,
                compounding = fd.compoundingFrequency
            )

            val fullInterest = InterestCalculator.calculateAccruedInterest(
                principal = fd.principalAmount,
                annualRate = applicableRate,
                valueDate = fd.valueDate,
                currentDate = today,
                compounding = fd.compoundingFrequency
            )

            val actualInterest = payoutAmount.subtract(fd.principalAmount)
            val penaltyAmount = fullInterest.subtract(actualInterest).max(BigDecimal.ZERO)
            val interestLoss = fd.estimatedMaturityAmount.subtract(payoutAmount)

            BreakFdRecommendation(
                fd = fd,
                elapsedDays = elapsedDays,
                applicableRate = applicableRate,
                penaltyRate = penaltyRate,
                effectiveRate = effectiveRate.setScale(2, RoundingMode.HALF_UP),
                payoutAmount = payoutAmount,
                penaltyAmount = penaltyAmount,
                interestLoss = interestLoss.max(BigDecimal.ZERO)
            )
        }.sortedBy { it.penaltyAmount }
    }
}
