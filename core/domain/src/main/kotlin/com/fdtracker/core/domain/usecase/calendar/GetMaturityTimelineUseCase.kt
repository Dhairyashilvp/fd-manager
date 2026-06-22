package com.fdtracker.core.domain.usecase.calendar

import com.fdtracker.core.common.toEpochMillis
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetMaturityTimelineUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<FixedDeposit>> {
        return fdRepository.observeByMaturityRange(
            from = startDate.toEpochMillis(),
            to = endDate.toEpochMillis()
        )
    }
}
