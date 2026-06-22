package com.fdtracker.core.domain.model

import java.math.BigDecimal

data class BankTdsStatus(
    val bankName: String,
    val totalProjectedInterest: BigDecimal,
    val tdsThreshold: BigDecimal,
    val isApproachingThreshold: Boolean,
    val isOverThreshold: Boolean,
    val percentageOfThreshold: BigDecimal
)
