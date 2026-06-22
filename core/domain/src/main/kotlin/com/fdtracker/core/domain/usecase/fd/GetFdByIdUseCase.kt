package com.fdtracker.core.domain.usecase.fd

import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow

class GetFdByIdUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(fdAccountNumber: String): Flow<FixedDeposit?> {
        return fdRepository.observeById(fdAccountNumber)
    }
}
