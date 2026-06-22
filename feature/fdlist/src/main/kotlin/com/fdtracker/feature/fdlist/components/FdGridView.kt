package com.fdtracker.feature.fdlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.ui.component.StatusBadge

@Composable
fun FdGridView(
    fds: List<FixedDeposit>,
    onFdClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(fds, key = { it.fdAccountNumber }) { fd ->
            Card(
                modifier = Modifier.clickable { onFdClick(fd.fdAccountNumber) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = fd.bankName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = fd.principalAmount.formatCurrency(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = fd.interestRatePA.formatPercent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fd.maturityDate.formatDisplay(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    StatusBadge(
                        maturityDate = fd.maturityDate,
                        gracePeriodDays = fd.gracePeriodDays,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
