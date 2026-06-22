package com.fdtracker.feature.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdExpired
import com.fdtracker.core.ui.theme.FdMaturing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyScreen(
    onNavigateBack: () -> Unit,
    viewModel: StrategyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Strategy & Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingShimmer(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Laddering Analysis
            uiState.ladderAnalysis?.let { analysis ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (analysis.isWellLaddered)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (analysis.isWellLaddered)
                                    Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (analysis.isWellLaddered) FdActive else FdMaturing
                            )
                            Text(
                                text = "FD Laddering Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Avg Gap", style = MaterialTheme.typography.labelSmall)
                                Text("${analysis.averageGapDays} days", style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text("Max Gap", style = MaterialTheme.typography.labelSmall)
                                Text("${analysis.maxGapDays} days", style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text("Min Gap", style = MaterialTheme.typography.labelSmall)
                                Text("${analysis.minGapDays} days", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = analysis.recommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Maturity Spread
                if (analysis.maturitySpread.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Maturity Spread", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            analysis.maturitySpread.forEach { point ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = point.maturityDate.formatDisplay(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${point.bankName} #${point.fdAccountNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = point.maturityAmount.formatCurrency(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (point.gapFromPrevious > 0) {
                                            Text(
                                                text = "+${point.gapFromPrevious}d",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Break FD Recommendation
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Which FD to Break?", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Enter the amount you need and we'll suggest which FD to break with minimum penalty.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.targetAmount,
                        onValueChange = { viewModel.onTargetAmountChanged(it) },
                        label = { Text("Target Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.calculateBreakRecommendations() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate")
                    }
                }
            }

            // Break FD Results
            uiState.breakRecommendations.forEach { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${rec.fd.bankName} #${rec.fd.fdAccountNumber}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Principal", style = MaterialTheme.typography.bodySmall)
                            Text(rec.fd.principalAmount.formatCurrency())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Elapsed", style = MaterialTheme.typography.bodySmall)
                            Text("${rec.elapsedDays} days")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Effective Rate", style = MaterialTheme.typography.bodySmall)
                            Text(rec.effectiveRate.formatPercent())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payout Amount", style = MaterialTheme.typography.bodySmall)
                            Text(rec.payoutAmount.formatCurrency())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Penalty", style = MaterialTheme.typography.bodySmall)
                            Text(
                                rec.penaltyAmount.formatCurrency(),
                                color = FdExpired
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Interest Loss", style = MaterialTheme.typography.bodySmall)
                            Text(
                                rec.interestLoss.formatCurrency(),
                                color = FdExpired
                            )
                        }
                    }
                }
            }
        }
    }
}
