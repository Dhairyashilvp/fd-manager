package com.fdtracker.feature.fddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.common.formatPercent
import com.fdtracker.core.ui.component.ConfirmDialog
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.core.ui.component.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: FdDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateBack()
    }

    if (uiState.showDeleteDialog) {
        ConfirmDialog(
            title = "Delete FD",
            message = "Are you sure you want to delete this Fixed Deposit? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = { viewModel.deleteFd() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FD Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.fd?.let { fd ->
                        IconButton(onClick = { onNavigateToEdit(fd.fdAccountNumber) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingShimmer(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val fd = uiState.fd ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = fd.bankName, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "FD #${fd.fdAccountNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(maturityDate = fd.maturityDate, gracePeriodDays = fd.gracePeriodDays)
            }

            // Financial Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financial Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Principal", fd.principalAmount.formatCurrency())
                    DetailRow("Interest Rate", fd.interestRatePA.formatPercent())
                    DetailRow("Compounding", fd.compoundingFrequency.name)
                    DetailRow("Maturity Amount", fd.estimatedMaturityAmount.formatCurrency())
                    DetailRow("Accrued Interest", uiState.accruedInterest.formatCurrency())
                    DetailRow("Auto Renewal", fd.autoRenewalInstruction.name.replace("_", " "))
                }
            }

            // Dates
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dates & Tenure", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Start Date", fd.valueDate.formatDisplay())
                    DetailRow("Maturity Date", fd.maturityDate.formatDisplay())
                    DetailRow("Tenure", "${fd.tenureDays} days")
                    DetailRow("Grace Period", "${fd.gracePeriodDays} days")
                }
            }

            // Holder Information
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Holder Information", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Primary Holder", fd.primaryHolderName)
                    DetailRow("Holding Mode", fd.holdingMode.name.replace("_", " "))
                    DetailRow("CIF/Customer ID", fd.cifCustomerId)
                    if (fd.jointHolderNames.isNotEmpty()) {
                        DetailRow("Joint Holders", fd.jointHolderNames.joinToString(", "))
                    }
                    fd.nomineeName?.let { DetailRow("Nominee", it) }
                    DetailRow("Payout Account", fd.payoutAccountId)
                }
            }

            // Optional Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Additional Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Category", fd.specialCategory.name.replace("_", " "))
                    DetailRow("Tax Saver", if (fd.isTaxSaver) "Yes" else "No")
                    DetailRow("TDS Applicable", fd.taxTdsApplicable?.let { if (it) "Yes" else "No" } ?: "N/A")
                    DetailRow("Tax Exemption", fd.taxExemptionForm.name.replace("_", " "))
                    fd.interestPayoutFrequency?.let { DetailRow("Payout Frequency", it.name) }
                    fd.branchCode?.let { DetailRow("Branch Code", it) }
                    fd.ifscCode?.let { DetailRow("IFSC Code", it) }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
