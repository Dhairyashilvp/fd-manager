package com.fdtracker.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdExpired
import com.fdtracker.core.ui.theme.FdMaturing
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun FdCard(
    fd: FixedDeposit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(fd.valueDate, fd.maturityDate).toFloat()
    val elapsed = ChronoUnit.DAYS.between(fd.valueDate, today).toFloat()
    val progress = if (totalDays > 0) (elapsed / totalDays).coerceIn(0f, 1f) else 1f
    val daysToMaturity = ChronoUnit.DAYS.between(today, fd.maturityDate)

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val progressColor = when {
        progress >= 1f -> FdExpired
        daysToMaturity <= 30 -> FdMaturing
        else -> FdActive
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = fd.bankName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "FD #${fd.fdAccountNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = fd.principalAmount.formatCurrency(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = fd.interestRatePA.formatPercent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Maturity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fd.maturityDate.formatDisplay(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Maturity Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fd.estimatedMaturityAmount.formatCurrency(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (daysToMaturity > 0) {
                Text(
                    text = "$daysToMaturity days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (daysToMaturity == 0L) {
                Text(
                    text = "Matures today!",
                    style = MaterialTheme.typography.labelSmall,
                    color = FdMaturing,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                val graceRemaining = fd.gracePeriodDays + daysToMaturity
                if (graceRemaining > 0) {
                    Text(
                        text = "Grace period: $graceRemaining days left",
                        style = MaterialTheme.typography.labelSmall,
                        color = FdMaturing,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Matured & grace period ended",
                        style = MaterialTheme.typography.labelSmall,
                        color = FdExpired,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
