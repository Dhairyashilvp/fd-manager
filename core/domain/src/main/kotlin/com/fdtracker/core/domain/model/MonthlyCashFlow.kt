package com.fdtracker.core.domain.model

import java.math.BigDecimal
import java.time.YearMonth

data class MonthlyCashFlow(
    val yearMonth: YearMonth,
    val totalMaturityAmount: BigDecimal,
    val fdCount: Int,
    val fdNumbers: List<String>
)
