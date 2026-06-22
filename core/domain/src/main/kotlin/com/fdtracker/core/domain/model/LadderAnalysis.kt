package com.fdtracker.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class LadderAnalysis(
    val isWellLaddered: Boolean,
    val averageGapDays: Int,
    val maxGapDays: Int,
    val minGapDays: Int,
    val maturitySpread: List<LadderPoint>,
    val recommendation: String
)

data class LadderPoint(
    val maturityDate: LocalDate,
    val fdAccountNumber: String,
    val bankName: String,
    val maturityAmount: BigDecimal,
    val gapFromPrevious: Int
)
