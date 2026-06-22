package com.fdtracker.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class MaturityResult(
    val maturityDate: LocalDate,
    val maturityAmount: BigDecimal,
    val totalInterest: BigDecimal
)
