package com.fdtracker.feature.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditFd: (
        bankName: String?,
        fdNumber: String?,
        principal: String?,
        rate: String?,
        valueDate: String?,
        maturityDate: String?,
        maturityAmount: String?,
        holderName: String?
    ) -> Unit,
    viewModel: OcrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val parsed = uiState.parsedFd

    var bankName by remember(parsed) { mutableStateOf(parsed?.bankName ?: "") }
    var fdNumber by remember(parsed) { mutableStateOf(parsed?.fdAccountNumber ?: "") }
    var principal by remember(parsed) { mutableStateOf(parsed?.principalAmount ?: "") }
    var rate by remember(parsed) { mutableStateOf(parsed?.interestRate ?: "") }
    var valueDate by remember(parsed) { mutableStateOf(parsed?.valueDate ?: "") }
    var maturityDate by remember(parsed) { mutableStateOf(parsed?.maturityDate ?: "") }
    var maturityAmount by remember(parsed) { mutableStateOf(parsed?.maturityAmount ?: "") }
    var holderName by remember(parsed) { mutableStateOf(parsed?.holderName ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scanned Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Confidence indicator
            parsed?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OCR Confidence: ${(it.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { it.confidence },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please review and correct any errors below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text("Extracted Fields", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fdNumber,
                onValueChange = { fdNumber = it },
                label = { Text("FD Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Holder Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Principal Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Interest Rate (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = valueDate,
                onValueChange = { valueDate = it },
                label = { Text("Start Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = maturityDate,
                onValueChange = { maturityDate = it },
                label = { Text("Maturity Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = maturityAmount,
                onValueChange = { maturityAmount = it },
                label = { Text("Maturity Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onNavigateToEditFd(
                        bankName.trim().ifBlank { null },
                        fdNumber.trim().ifBlank { null },
                        principal.trim().ifBlank { null },
                        rate.trim().ifBlank { null },
                        valueDate.trim().ifBlank { null },
                        maturityDate.trim().ifBlank { null },
                        maturityAmount.trim().ifBlank { null },
                        holderName.trim().ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to Add FD")
            }
        }
    }
}
