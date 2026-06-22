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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.domain.model.HoldingMode
import com.fdtracker.core.domain.model.PayoutFrequency
import com.fdtracker.core.domain.model.RenewalInstruction
import com.fdtracker.core.domain.model.SpecialCategory
import com.fdtracker.core.domain.model.TaxExemptionForm
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFdScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditFdViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit FD" else "Add New FD") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mandatory Fields Section
            Text("Mandatory Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.fdAccountNumber,
                onValueChange = { viewModel.updateField("fdAccountNumber", it) },
                label = { Text("FD Account Number *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isEditMode
            )

            OutlinedTextField(
                value = uiState.bankName,
                onValueChange = { viewModel.updateField("bankName", it) },
                label = { Text("Bank Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.cifCustomerId,
                onValueChange = { viewModel.updateField("cifCustomerId", it) },
                label = { Text("Customer ID / CIF *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.primaryHolderName,
                onValueChange = { viewModel.updateField("primaryHolderName", it) },
                label = { Text("Primary Holder Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            // Holding Mode dropdown
            EnumDropdown(
                label = "Mode of Holding",
                selected = uiState.holdingMode,
                values = HoldingMode.entries.toTypedArray(),
                onSelected = { viewModel.updateField("holdingMode", it) }
            )

            if (uiState.holdingMode != HoldingMode.SINGLE) {
                OutlinedTextField(
                    value = uiState.jointHolderNames,
                    onValueChange = { viewModel.updateField("jointHolderNames", it) },
                    label = { Text("Joint Holder Names (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = uiState.principalAmount,
                onValueChange = { viewModel.updateField("principalAmount", it) },
                label = { Text("Principal Amount *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₹") }
            )

            // Start Date (Value Date) picker
            var showDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.valueDate.formatDisplay(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Date (Value Date) *") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select start date")
                    }
                }
            )
            if (showDatePicker) {
                val initialMillis = uiState.valueDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialMillis
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selected = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                viewModel.updateValueDate(selected)
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = uiState.interestRatePA,
                onValueChange = { viewModel.updateField("interestRatePA", it) },
                label = { Text("Interest Rate (% P.A.) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("%") }
            )

            OutlinedTextField(
                value = uiState.tenureDays,
                onValueChange = { viewModel.updateField("tenureDays", it) },
                label = { Text("Tenure (Days) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            EnumDropdown(
                label = "Compounding Frequency",
                selected = uiState.compoundingFrequency,
                values = PayoutFrequency.entries.toTypedArray(),
                onSelected = { viewModel.updateField("compoundingFrequency", it) }
            )

            // Auto-calculated fields
            val maturityAmt = uiState.estimatedMaturityAmount.toBigDecimalOrNull()
            if (maturityAmt != null) {
                Text(
                    text = "Estimated Maturity: ${maturityAmt.formatCurrency()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Maturity Date: ${uiState.maturityDate.formatDisplay()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EnumDropdown(
                label = "Auto Renewal Instruction",
                selected = uiState.autoRenewalInstruction,
                values = RenewalInstruction.entries.toTypedArray(),
                onSelected = { viewModel.updateField("autoRenewalInstruction", it) }
            )

            OutlinedTextField(
                value = uiState.payoutAccountId,
                onValueChange = { viewModel.updateField("payoutAccountId", it) },
                label = { Text("Payout Account ID *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.gracePeriodDays,
                onValueChange = { viewModel.updateField("gracePeriodDays", it) },
                label = { Text("Grace Period (Days)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Optional Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.nomineeName,
                onValueChange = { viewModel.updateField("nomineeName", it) },
                label = { Text("Nominee Name") },
                modifier = Modifier.fillMaxWidth()
            )

            EnumDropdown(
                label = "Special Category",
                selected = uiState.specialCategory,
                values = SpecialCategory.entries.toTypedArray(),
                onSelected = { viewModel.updateField("specialCategory", it) }
            )

            EnumDropdown(
                label = "Tax Exemption Form",
                selected = uiState.taxExemptionForm,
                values = TaxExemptionForm.entries.toTypedArray(),
                onSelected = { viewModel.updateField("taxExemptionForm", it) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tax Saver FD")
                Switch(
                    checked = uiState.isTaxSaver,
                    onCheckedChange = { viewModel.updateField("isTaxSaver", it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TDS Applicable")
                Switch(
                    checked = uiState.taxTdsApplicable,
                    onCheckedChange = { viewModel.updateField("taxTdsApplicable", it) }
                )
            }

            OutlinedTextField(
                value = uiState.branchCode,
                onValueChange = { viewModel.updateField("branchCode", it) },
                label = { Text("Branch Code") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.ifscCode,
                onValueChange = { viewModel.updateField("ifscCode", it) },
                label = { Text("IFSC Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isEditMode) "Update FD" else "Save FD")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private inline fun <reified T : Enum<T>> EnumDropdown(
    label: String,
    selected: T,
    values: Array<T>,
    crossinline onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.name.replace("_", " ")) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
