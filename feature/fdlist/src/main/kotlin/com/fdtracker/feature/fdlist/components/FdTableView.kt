package com.fdtracker.feature.fdlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.domain.model.FixedDeposit

@Composable
fun FdTableView(
    fds: List<FixedDeposit>,
    onFdClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        // Header
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            TableCell("Bank", Modifier.weight(1.5f), isHeader = true)
            TableCell("FD No.", Modifier.weight(1.5f), isHeader = true)
            TableCell("Principal", Modifier.weight(1.5f), isHeader = true)
            TableCell("Rate", Modifier.weight(1f), isHeader = true)
            TableCell("Maturity", Modifier.weight(1.5f), isHeader = true)
            TableCell("Amount", Modifier.weight(1.5f), isHeader = true)
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(fds, key = { it.fdAccountNumber }) { fd ->
                Row(
                    modifier = Modifier
                        .clickable { onFdClick(fd.fdAccountNumber) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TableCell(fd.bankName, Modifier.weight(1.5f))
                    TableCell(fd.fdAccountNumber, Modifier.weight(1.5f))
                    TableCell(fd.principalAmount.formatCurrency(), Modifier.weight(1.5f))
                    TableCell(fd.interestRatePA.formatPercent(), Modifier.weight(1f))
                    TableCell(fd.maturityDate.formatDisplay(), Modifier.weight(1.5f))
                    TableCell(fd.estimatedMaturityAmount.formatCurrency(), Modifier.weight(1.5f))
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier, isHeader: Boolean = false) {
    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        else MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(4.dp)
    )
}
