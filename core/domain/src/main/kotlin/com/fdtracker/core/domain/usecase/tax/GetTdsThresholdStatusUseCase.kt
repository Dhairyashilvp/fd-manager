package com.fdtracker.core.domain.usecase.tax

import com.fdtracker.core.common.Constants
import com.fdtracker.core.common.financialYearEnd
import com.fdtracker.core.common.financialYearStart
import com.fdtracker.core.domain.model.BankTdsStatus
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.util.InterestCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class GetTdsThresholdStatusUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(isSeniorCitizen: Boolean = false): Flow<List<BankTdsStatus>> {
        return fdRepository.observeAllActive().map { fds ->
            val today = LocalDate.now()
            val fyStart = today.financialYearStart()
            val fyEnd = today.financialYearEnd()
            val threshold = if (isSeniorCitizen) Constants.TDS_THRESHOLD_SENIOR_CITIZEN
                           else Constants.TDS_THRESHOLD_GENERAL

            fds.groupBy { it.bankName }.map { (bankName, bankFds) ->
                val totalProjectedInterest = bankFds.fold(BigDecimal.ZERO) { acc, fd ->
                    // Calculate interest for the FY period overlap
                    val fdStart = maxOf(fd.valueDate, fyStart)
                    val fdEnd = minOf(fd.maturityDate, fyEnd)
                    if (fdStart.isAfter(fdEnd)) return@fold acc

                    val interest = InterestCalculator.calculateAccruedInterest(
                        principal = fd.principalAmount,
                        annualRate = fd.interestRatePA,
                        valueDate = fd.valueDate,
                        currentDate = fdEnd,
                        compounding = fd.compoundingFrequency
                    )
                    // Subtract interest accrued before FY start
                    val priorInterest = if (fd.valueDate.isBefore(fyStart)) {
                        InterestCalculator.calculateAccruedInterest(
                            principal = fd.principalAmount,
                            annualRate = fd.interestRatePA,
                            valueDate = fd.valueDate,
                            currentDate = fyStart,
                            compounding = fd.compoundingFrequency
                        )
                    } else BigDecimal.ZERO

                    acc.add(interest.subtract(priorInterest))
                }

                val percentage = if (threshold > BigDecimal.ZERO) {
                    totalProjectedInterest.multiply(BigDecimal(100))
                        .divide(threshold, 2, RoundingMode.HALF_UP)
                } else BigDecimal.ZERO

                BankTdsStatus(
                    bankName = bankName,
                    totalProjectedInterest = totalProjectedInterest,
                    tdsThreshold = threshold,
                    isApproachingThreshold = percentage >= BigDecimal(80) && percentage < BigDecimal(100),
                    isOverThreshold = totalProjectedInterest >= threshold,
                    percentageOfThreshold = percentage
                )
            }.sortedByDescending { it.percentageOfThreshold }
        }
    }
}
