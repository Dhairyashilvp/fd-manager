package com.fdtracker.core.domain.usecase.dashboard

import com.fdtracker.core.common.Constants
import com.fdtracker.core.domain.model.BankExposure
import com.fdtracker.core.domain.repository.BankRepository
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

class GetBankExposureUseCase(
    private val fdRepository: FdRepository,
    private val bankRepository: BankRepository
) {
    operator fun invoke(): Flow<List<BankExposure>> {
        return fdRepository.observeAllActive().map { fds ->
            val grouped = fds.groupBy { it.bankName }
            val totalPrincipal = fds.fold(BigDecimal.ZERO) { acc, fd -> acc.add(fd.principalAmount) }

            grouped.map { (bankName, bankFds) ->
                val bankPrincipal = bankFds.fold(BigDecimal.ZERO) { acc, fd -> acc.add(fd.principalAmount) }
                val insuranceLimit = try {
                    bankRepository.getInsuranceLimit(bankName)
                } catch (_: Exception) {
                    Constants.DICGC_INSURANCE_LIMIT
                }

                val percentage = if (totalPrincipal > BigDecimal.ZERO) {
                    bankPrincipal.multiply(BigDecimal(100))
                        .divide(totalPrincipal, 2, RoundingMode.HALF_UP)
                } else BigDecimal.ZERO

                BankExposure(
                    bankName = bankName,
                    totalPrincipal = bankPrincipal,
                    fdCount = bankFds.size,
                    percentageOfPortfolio = percentage,
                    insuranceLimit = insuranceLimit,
                    isOverInsuranceLimit = bankPrincipal > insuranceLimit
                )
            }.sortedByDescending { it.totalPrincipal }
        }
    }
}
