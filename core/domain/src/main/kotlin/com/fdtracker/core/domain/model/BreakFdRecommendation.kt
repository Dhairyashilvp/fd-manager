package com.fdtracker.core.domain.model

import java.math.BigDecimal

data class BreakFdRecommendation(
    val fd: FixedDeposit,
    val elapsedDays: Int,
    val applicableRate: BigDecimal,
    val penaltyRate: BigDecimal,
    val effectiveRate: BigDecimal,
    val payoutAmount: BigDecimal,
    val penaltyAmount: BigDecimal,
    val interestLoss: BigDecimal
)
