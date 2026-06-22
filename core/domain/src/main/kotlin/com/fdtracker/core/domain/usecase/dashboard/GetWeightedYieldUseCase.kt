package com.fdtracker.core.domain.usecase.dashboard

import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.util.InterestCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class GetWeightedYieldUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(): Flow<BigDecimal> {
        return fdRepository.observeAllActive().map { fds ->
            if (fds.isEmpty()) return@map BigDecimal.ZERO
            InterestCalculator.calculateWeightedAverageYield(
                principals = fds.map { it.principalAmount },
                rates = fds.map { it.interestRatePA }
            )
        }
    }
}
