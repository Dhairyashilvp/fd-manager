package com.fdtracker.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.ui.component.LoadingShimmer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDobPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedMessage, uiState.error) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            // User Profile
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("User Profile", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.userName,
                        onValueChange = { viewModel.updateField("userName", it) },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.dateOfBirth?.toString() ?: "",
                        onValueChange = {},
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDobPicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select date of birth"
                                )
                            }
                        }
                    )

                    if (showDobPicker) {
                        val defaultDob = uiState.dateOfBirth ?: LocalDate.now().minusYears(30)
                        val initialMillis = defaultDob
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                            .toEpochMilli()
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = initialMillis,
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    return utcTimeMillis <= System.currentTimeMillis()
                                }

                                override fun isSelectableYear(year: Int): Boolean {
                                    return year <= LocalDate.now().year
                                }
                            }
                        )

                        DatePickerDialog(
                            onDismissRequest = { showDobPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedDate = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneOffset.UTC)
                                            .toLocalDate()
                                        viewModel.updateDateOfBirth(selectedDate)
                                    }
                                    showDobPicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDobPicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    OutlinedTextField(
                        value = uiState.panNumber,
                        onValueChange = { viewModel.updateField("panNumber", it) },
                        label = { Text("PAN Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }
                }
            }

            // Notifications
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggle(
                        title = "Push Notifications",
                        subtitle = "Get notified before FD maturities",
                        checked = uiState.pushReminders,
                        onCheckedChange = { viewModel.togglePushReminders(it) }
                    )

                    HorizontalDivider()

                    SettingsToggle(
                        title = "Email Reminders",
                        subtitle = "Send email reminders via SMTP",
                        checked = uiState.emailReminders,
                        onCheckedChange = { viewModel.toggleEmailReminders(it) }
                    )
                }
            }

            // Security
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggle(
                        title = "App Lock",
                        subtitle = "Require biometric authentication",
                        checked = uiState.appLock,
                        onCheckedChange = { viewModel.toggleAppLock(it) }
                    )
                }
            }

            // Appearance
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggle(
                        title = "Dark Mode",
                        subtitle = "Toggle dark theme",
                        checked = uiState.darkMode ?: false,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }

            // SMTP Configuration
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSmtpSection() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SMTP Configuration", style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = if (uiState.showSmtpSection) Icons.Default.ExpandLess
                            else Icons.Default.ExpandMore,
                            contentDescription = "Toggle"
                        )
                    }

                    AnimatedVisibility(visible = uiState.showSmtpSection) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = uiState.smtpHost,
                                onValueChange = { viewModel.updateField("smtpHost", it) },
                                label = { Text("SMTP Host") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.smtpPort,
                                onValueChange = { viewModel.updateField("smtpPort", it) },
                                label = { Text("Port") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = uiState.smtpUsername,
                                onValueChange = { viewModel.updateField("smtpUsername", it) },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.smtpPassword,
                                onValueChange = { viewModel.updateField("smtpPassword", it) },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            OutlinedTextField(
                                value = uiState.smtpFromAddress,
                                onValueChange = { viewModel.updateField("smtpFromAddress", it) },
                                label = { Text("From Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.smtpToAddress,
                                onValueChange = { viewModel.updateField("smtpToAddress", it) },
                                label = { Text("To Email") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            SettingsToggle(
                                title = "Use TLS",
                                subtitle = "Enable TLS encryption",
                                checked = uiState.smtpUseTls,
                                onCheckedChange = { viewModel.toggleSmtpTls(it) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.saveSmtpConfig() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save SMTP Config")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
