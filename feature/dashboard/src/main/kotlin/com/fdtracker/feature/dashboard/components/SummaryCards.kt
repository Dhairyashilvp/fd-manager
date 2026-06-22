package com.fdtracker.feature.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.ui.component.KpiCard
import java.math.BigDecimal

@Composable
fun SummaryCards(
    totalPrincipal: BigDecimal,
    totalAccrued: BigDecimal,
    totalMaturityValue: BigDecimal,
    weightedYield: BigDecimal,
    activeFdCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            title = "Total Invested",
            value = totalPrincipal.formatCurrency(),
            subtitle = "$activeFdCount active FDs",
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            title = "Accrued Interest",
            value = totalAccrued.formatCurrency(),
            subtitle = "Yield: ${weightedYield.formatPercent()}",
            modifier = Modifier.weight(1f)
        )
    }
}
