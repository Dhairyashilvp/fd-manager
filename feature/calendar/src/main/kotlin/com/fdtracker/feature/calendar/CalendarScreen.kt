package com.fdtracker.feature.calendar

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.common.formatDisplay
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.feature.calendar.components.CashFlowChart
import com.fdtracker.feature.calendar.components.MaturityCalendar
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToFd: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Maturity Calendar") })
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
            // Month navigation
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.onMonthChanged(uiState.currentMonth.minusMonths(1))
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous month")
                        }
                        Text(
                            text = "${uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${uiState.currentMonth.year}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = {
                            viewModel.onMonthChanged(uiState.currentMonth.plusMonths(1))
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next month")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MaturityCalendar(
                        currentMonth = uiState.currentMonth,
                        maturityDates = uiState.maturityDates,
                        selectedDate = uiState.selectedDate,
                        onDateSelected = { viewModel.onDateSelected(it) }
                    )
                }
            }

            // Selected date FDs
            if (uiState.selectedDateFds.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FDs maturing on ${uiState.selectedDate?.formatDisplay()}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.selectedDateFds.forEach { fd ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${fd.bankName} #${fd.fdAccountNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = fd.estimatedMaturityAmount.formatCurrency(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Cash flow forecast chart
            CashFlowChart(cashFlows = uiState.cashFlowForecast)
        }
    }
}
