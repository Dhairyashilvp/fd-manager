package com.fdtracker.core.domain.usecase.dashboard

import com.fdtracker.core.domain.model.DashboardSummary
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.usecase.calculation.CalculateAccruedInterestUseCase
import com.fdtracker.core.domain.util.InterestCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate

class GetDashboardSummaryUseCase(
    private val fdRepository: FdRepository,
    private val calculateAccruedInterest: CalculateAccruedInterestUseCase
) {
    operator fun invoke(): Flow<DashboardSummary> {
        return fdRepository.observeAllActive().map { fds ->
            val today = LocalDate.now()

            val totalPrincipal = fds.fold(BigDecimal.ZERO) { acc, fd ->
                acc.add(fd.principalAmount)
            }

            val totalAccrued = fds.fold(BigDecimal.ZERO) { acc, fd ->
                acc.add(calculateAccruedInterest(fd, today))
            }

            val totalMaturityValue = fds.fold(BigDecimal.ZERO) { acc, fd ->
                acc.add(fd.estimatedMaturityAmount)
            }

            val weightedYield = if (fds.isNotEmpty()) {
                InterestCalculator.calculateWeightedAverageYield(
                    principals = fds.map { it.principalAmount },
                    rates = fds.map { it.interestRatePA }
                )
            } else BigDecimal.ZERO

            DashboardSummary(
                totalPrincipal = totalPrincipal,
                totalAccruedInterest = totalAccrued,
                totalMaturityValue = totalMaturityValue,
                activeFdCount = fds.size,
                weightedAverageYield = weightedYield
            )
        }
    }
}
