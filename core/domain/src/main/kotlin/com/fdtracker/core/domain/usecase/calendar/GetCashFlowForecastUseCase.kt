package com.fdtracker.core.domain.usecase.calendar

import com.fdtracker.core.domain.model.MonthlyCashFlow
import com.fdtracker.core.domain.repository.FdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class GetCashFlowForecastUseCase(
    private val fdRepository: FdRepository
) {
    operator fun invoke(months: Int = 24): Flow<List<MonthlyCashFlow>> {
        return fdRepository.observeAllActive().map { fds ->
            val today = LocalDate.now()
            val startMonth = YearMonth.from(today)
            val endMonth = startMonth.plusMonths(months.toLong())

            val monthlyMap = mutableMapOf<YearMonth, MutableList<Pair<String, BigDecimal>>>()

            fds.filter { it.maturityDate.isAfter(today) || YearMonth.from(it.maturityDate) == startMonth }
                .forEach { fd ->
                    val ym = YearMonth.from(fd.maturityDate)
                    if (ym in startMonth..endMonth) {
                        monthlyMap.getOrPut(ym) { mutableListOf() }
                            .add(fd.fdAccountNumber to fd.estimatedMaturityAmount)
                    }
                }

            // Generate all months in range, filling empty ones with zero
            generateSequence(startMonth) { it.plusMonths(1) }
                .takeWhile { it <= endMonth }
                .map { ym ->
                    val entries = monthlyMap[ym] ?: emptyList()
                    MonthlyCashFlow(
                        yearMonth = ym,
                        totalMaturityAmount = entries.fold(BigDecimal.ZERO) { acc, (_, amt) -> acc.add(amt) },
                        fdCount = entries.size,
                        fdNumbers = entries.map { it.first }
                    )
                }.toList()
        }
    }
}
