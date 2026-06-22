package com.fdtracker.feature.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.ui.component.StatusBadge

@Composable
fun UpcomingMaturities(
    maturities: List<FixedDeposit>,
    onFdClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Upcoming Maturities",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (maturities.isEmpty()) {
                Text(
                    text = "No FDs maturing in the next 30 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                maturities.forEachIndexed { index, fd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFdClick(fd.fdAccountNumber) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fd.bankName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = fd.maturityDate.formatDisplay(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = fd.estimatedMaturityAmount.formatCurrency(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            StatusBadge(
                                maturityDate = fd.maturityDate,
                                gracePeriodDays = fd.gracePeriodDays
                            )
                        }
                    }
                    if (index < maturities.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
