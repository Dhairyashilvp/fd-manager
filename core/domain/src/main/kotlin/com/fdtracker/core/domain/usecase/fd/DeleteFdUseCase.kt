package com.fdtracker.core.domain.usecase.fd

import com.fdtracker.core.common.Result
import com.fdtracker.core.domain.repository.FdRepository

class DeleteFdUseCase(
    private val fdRepository: FdRepository
) {
    suspend operator fun invoke(fdAccountNumber: String): Result<Unit> {
        return Result.runCatching {
            require(fdAccountNumber.isNotBlank()) { "FD Account Number is required" }
            fdRepository.delete(fdAccountNumber)
        }
    }
}
