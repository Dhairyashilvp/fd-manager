package com.fdtracker.core.domain.usecase.dashboard

import com.fdtracker.core.domain.model.BankExposure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckInsuranceLimitUseCase(
    private val getBankExposure: GetBankExposureUseCase
) {
    operator fun invoke(): Flow<List<BankExposure>> {
        return getBankExposure().map { exposures ->
            exposures.filter { it.isOverInsuranceLimit }
        }
    }
}
