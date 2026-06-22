package com.fdtracker.core.domain.model

import java.math.BigDecimal

data class DashboardSummary(
    val totalPrincipal: BigDecimal,
    val totalAccruedInterest: BigDecimal,
    val totalMaturityValue: BigDecimal,
    val activeFdCount: Int,
    val weightedAverageYield: BigDecimal
)
