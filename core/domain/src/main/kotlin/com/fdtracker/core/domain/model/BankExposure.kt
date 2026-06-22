package com.fdtracker.core.domain.model

import java.math.BigDecimal

data class BankExposure(
    val bankName: String,
    val totalPrincipal: BigDecimal,
    val fdCount: Int,
    val percentageOfPortfolio: BigDecimal,
    val insuranceLimit: BigDecimal,
    val isOverInsuranceLimit: Boolean
)
