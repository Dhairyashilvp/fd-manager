package com.fdtracker.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fdtracker.core.domain.model.MonthlyCashFlow
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.util.CurrencyFormatter
import java.math.BigDecimal

@Composable
fun CashFlowChart(
    cashFlows: List<MonthlyCashFlow>,
    modifier: Modifier = Modifier
) {
    val nonZero = cashFlows.filter { it.totalMaturityAmount > BigDecimal.ZERO }
    if (nonZero.isEmpty()) return

    val maxAmount = nonZero.maxOf { it.totalMaturityAmount }.toFloat()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cash Flow Forecast", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                nonZero.take(24).forEach { cashFlow ->
                    val barHeight = if (maxAmount > 0) {
                        (cashFlow.totalMaturityAmount.toFloat() / maxAmount * 150f).coerceAtLeast(8f)
                    } else 8f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatCompact(cashFlow.totalMaturityAmount),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(FdActive)
                        )
                        Text(
                            text = "${cashFlow.yearMonth.month.name.take(3)}\n${cashFlow.yearMonth.year}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
