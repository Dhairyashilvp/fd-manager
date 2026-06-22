package com.fdtracker.feature.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.core.ui.theme.FdActive
import com.fdtracker.core.ui.theme.FdExpired
import com.fdtracker.core.ui.theme.FdMaturing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tax Optimization") },
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
            // TDS Threshold Status
            Text("TDS Threshold Status", style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (uiState.isSeniorCitizen) "Senior Citizen (Threshold: ₹50,000)" else "General (Threshold: ₹40,000)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.tdsStatus.forEach { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.isOverThreshold -> MaterialTheme.colorScheme.errorContainer
                            status.isApproachingThreshold -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = status.bankName, style = MaterialTheme.typography.titleMedium)
                            Icon(
                                imageVector = if (status.isOverThreshold) Icons.Default.Warning
                                else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = when {
                                    status.isOverThreshold -> FdExpired
                                    status.isApproachingThreshold -> FdMaturing
                                    else -> FdActive
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Projected Interest", style = MaterialTheme.typography.bodySmall)
                            Text(
                                status.totalProjectedInterest.formatCurrency(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Threshold Usage", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${status.percentageOfThreshold}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = {
                                (status.percentageOfThreshold.toFloat() / 100f).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                status.isOverThreshold -> FdExpired
                                status.isApproachingThreshold -> FdMaturing
                                else -> FdActive
                            }
                        )
                    }
                }
            }

            // Form 15G/H Checklist
            if (uiState.form15GChecklist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Form 15G/H Checklist", style = MaterialTheme.typography.titleLarge)

                uiState.form15GChecklist.forEach { action ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (action.isSubmitted) FdActive.copy(alpha = 0.2f)
                                        else if (action.isRequired) FdExpired.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (action.isSubmitted) Icons.Default.CheckCircle
                                    else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (action.isSubmitted) FdActive else FdExpired,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "${action.bankName} - ${action.formType.name.replace("_", " ")}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Projected Interest: ${action.projectedInterest.formatCurrency()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = when {
                                        action.isSubmitted -> "Submitted"
                                        action.isRequired -> "Required - Not Submitted"
                                        else -> "Not Required"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        action.isSubmitted -> FdActive
                                        action.isRequired -> FdExpired
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
