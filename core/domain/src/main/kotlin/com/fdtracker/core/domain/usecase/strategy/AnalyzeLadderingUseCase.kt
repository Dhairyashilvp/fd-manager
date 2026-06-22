package com.fdtracker.core.domain.usecase.strategy

import com.fdtracker.core.domain.model.LadderAnalysis
import com.fdtracker.core.domain.model.LadderPoint
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AnalyzeLadderingUseCase(
    private val fdRepository: FdRepository
) {
    suspend operator fun invoke(): LadderAnalysis {
        val fds = fdRepository.observeAllActive().first()
            .filter { it.maturityDate.isAfter(LocalDate.now()) }
            .sortedBy { it.maturityDate }

        if (fds.isEmpty()) {
            return LadderAnalysis(
                isWellLaddered = false,
                averageGapDays = 0,
                maxGapDays = 0,
                minGapDays = 0,
                maturitySpread = emptyList(),
                recommendation = "No active FDs with future maturity dates found."
            )
        }

        val points = fds.mapIndexed { index, fd ->
            val gap = if (index > 0) {
                ChronoUnit.DAYS.between(fds[index - 1].maturityDate, fd.maturityDate).toInt()
            } else 0

            LadderPoint(
                maturityDate = fd.maturityDate,
                fdAccountNumber = fd.fdAccountNumber,
                bankName = fd.bankName,
                maturityAmount = fd.estimatedMaturityAmount,
                gapFromPrevious = gap
            )
        }

        val gaps = points.drop(1).map { it.gapFromPrevious }
        val avgGap = if (gaps.isNotEmpty()) gaps.average().toInt() else 0
        val maxGap = gaps.maxOrNull() ?: 0
        val minGap = gaps.minOrNull() ?: 0

        // Consider well-laddered if max gap < 120 days (4 months) and gaps are relatively even
        val isWellLaddered = maxGap <= 120 && (maxGap - minGap) < 60

        val recommendation = when {
            fds.size < 3 -> "Consider creating more FDs with staggered maturity dates for better liquidity."
            !isWellLaddered && maxGap > 180 -> "There's a gap of $maxGap days between maturities. Consider adding an FD maturing in between for steady cash flow."
            !isWellLaddered -> "Maturity gaps are uneven (${minGap}d–${maxGap}d). Aim for consistent 90-day intervals."
            else -> "Your FD portfolio is well-laddered with maturities spread approximately every ${avgGap} days."
        }

        return LadderAnalysis(
            isWellLaddered = isWellLaddered,
            averageGapDays = avgGap,
            maxGapDays = maxGap,
            minGapDays = minGap,
            maturitySpread = points,
            recommendation = recommendation
        )
    }
}
