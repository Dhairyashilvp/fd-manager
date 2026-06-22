package com.fdtracker.core.domain.usecase.fd

import com.fdtracker.core.common.Result
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.BankInfo
import com.fdtracker.core.domain.repository.BankRepository
import com.fdtracker.core.domain.repository.FdRepository
import java.math.BigDecimal

class AddOrUpdateFdUseCase(
    private val fdRepository: FdRepository,
    private val bankRepository: BankRepository
) {
    suspend operator fun invoke(fd: FixedDeposit): Result<Unit> {
        return Result.runCatching {
            // Validate required fields
            require(fd.fdAccountNumber.isNotBlank()) { "FD Account Number is required" }
            require(fd.bankName.isNotBlank()) { "Bank Name is required" }
            require(fd.principalAmount > BigDecimal.ZERO) { "Principal amount must be positive" }
            require(fd.interestRatePA > BigDecimal.ZERO) { "Interest rate must be positive" }
            require(fd.tenureDays > 0) { "Tenure must be positive" }
            require(!fd.maturityDate.isBefore(fd.valueDate)) { "Maturity date must be after value date" }

            // Ensure bank exists
            bankRepository.upsert(BankInfo(bankName = fd.bankName))

            fdRepository.upsert(fd)
        }
    }
}
