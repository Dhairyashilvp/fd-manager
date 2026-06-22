package com.fdtracker.core.domain.usecase.fd

import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow

class GetAllActiveFdsUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(): Flow<List<FixedDeposit>> {
        return fdRepository.observeAllActive()
    }
}
