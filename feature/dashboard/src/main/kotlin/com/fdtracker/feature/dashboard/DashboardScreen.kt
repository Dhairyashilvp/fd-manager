package com.fdtracker.feature.dashboard

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fdtracker.core.common.formatCurrency
import com.fdtracker.core.ui.component.InsuranceLimitBanner
import com.fdtracker.core.ui.component.KpiCard
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.feature.dashboard.components.BankHeatmap
import com.fdtracker.feature.dashboard.components.SummaryCards
import com.fdtracker.feature.dashboard.components.UpcomingMaturities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFd: (String) -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToTax: () -> Unit,
    onNavigateToStrategy: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navEvent by viewModel.navigateEvent.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(navEvent) {
        when (val event = navEvent) {
            is DashboardEvent.NavigateToFd -> {
                onNavigateToFd(event.fdId)
                viewModel.consumeNavigationEvent()
            }
            is DashboardEvent.NavigateToOcr -> {
                onNavigateToOcr()
                viewModel.consumeNavigationEvent()
            }
            is DashboardEvent.NavigateToTax -> {
                onNavigateToTax()
                viewModel.consumeNavigationEvent()
            }
            is DashboardEvent.NavigateToStrategy -> {
                onNavigateToStrategy()
                viewModel.consumeNavigationEvent()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FD Tracker") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(DashboardEvent.NavigateToTax) }) {
                        Icon(Icons.Default.Receipt, contentDescription = "Tax")
                    }
                    IconButton(onClick = { viewModel.onEvent(DashboardEvent.NavigateToStrategy) }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Strategy")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onEvent(DashboardEvent.NavigateToOcr) }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan FD")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingShimmer(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InsuranceLimitBanner(overLimitBanks = uiState.overLimitBanks)

                SummaryCards(
                    totalPrincipal = uiState.totalPrincipal,
                    totalAccrued = uiState.totalAccrued,
                    totalMaturityValue = uiState.totalMaturityValue,
                    weightedYield = uiState.weightedYield,
                    activeFdCount = uiState.activeFdCount
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Maturity Value",
                        value = uiState.totalMaturityValue.formatCurrency(),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Avg Yield",
                        value = "${uiState.weightedYield}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                BankHeatmap(exposures = uiState.bankExposures)

                UpcomingMaturities(
                    maturities = uiState.upcomingMaturities,
                    onFdClick = { viewModel.onEvent(DashboardEvent.NavigateToFd(it)) }
                )
            }
        }
    }
}
