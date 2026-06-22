package com.fdtracker.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.domain.model.BankExposure
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdExpired
import com.fdtracker.core.ui.theme.FdMaturing

@Composable
fun BankHeatmap(
    exposures: List<BankExposure>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bank-Wise Exposure",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            exposures.forEach { exposure ->
                val barColor = when {
                    exposure.isOverInsuranceLimit -> FdExpired
                    exposure.percentageOfPortfolio.toFloat() > 50f -> FdMaturing
                    else -> FdActive
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exposure.bankName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${exposure.fdCount} FDs | ${exposure.percentageOfPortfolio}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = exposure.totalPrincipal.formatCurrency(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(exposure.percentageOfPortfolio.toFloat() / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
