package com.fdtracker.feature.fdlist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.fdtracker.core.ui.component.BankChip
import com.fdtracker.core.ui.component.EmptyState
import com.fdtracker.core.ui.component.LoadingShimmer
import com.fdtracker.feature.fdlist.components.FdCardView
import com.fdtracker.feature.fdlist.components.FdGridView
import com.fdtracker.feature.fdlist.components.FdListView
import com.fdtracker.feature.fdlist.components.FdTableView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddFd: () -> Unit,
    viewModel: FdListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fixed Deposits") },
                actions = {
                    // View mode toggles
                    IconButton(onClick = { viewModel.onEvent(FdListEvent.SetViewMode(ViewMode.LIST)) }) {
                        Icon(Icons.Default.List, contentDescription = "List view")
                    }
                    IconButton(onClick = { viewModel.onEvent(FdListEvent.SetViewMode(ViewMode.TABLE)) }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Table view")
                    }
                    IconButton(onClick = { viewModel.onEvent(FdListEvent.SetViewMode(ViewMode.CARD)) }) {
                        Icon(Icons.Default.ViewAgenda, contentDescription = "Card view")
                    }
                    IconButton(onClick = { viewModel.onEvent(FdListEvent.SetViewMode(ViewMode.GRID)) }) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid view")
                    }

                    // Sort menu
                    TextButton(onClick = { showSortMenu = true }) {
                        Text("Sort")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Maturity Date") },
                            onClick = {
                                viewModel.onEvent(FdListEvent.SetSortBy(SortBy.MATURITY_DATE))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Principal Amount") },
                            onClick = {
                                viewModel.onEvent(FdListEvent.SetSortBy(SortBy.PRINCIPAL))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Interest Rate") },
                            onClick = {
                                viewModel.onEvent(FdListEvent.SetSortBy(SortBy.INTEREST_RATE))
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFd) {
                Icon(Icons.Default.Add, contentDescription = "Add FD")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingShimmer(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        if (uiState.fds.isEmpty()) {
            EmptyState(
                title = "No Fixed Deposits",
                message = "Add your first FD to start tracking",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Bank filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BankChip(
                    bankName = "All",
                    selected = uiState.selectedBank == null,
                    onClick = { viewModel.onEvent(FdListEvent.FilterByBank(null)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                uiState.bankNames.forEach { bank ->
                    BankChip(
                        bankName = bank,
                        selected = uiState.selectedBank == bank,
                        onClick = { viewModel.onEvent(FdListEvent.FilterByBank(bank)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // View content
            when (uiState.viewMode) {
                ViewMode.LIST -> FdListView(
                    fds = uiState.filteredFds,
                    onFdClick = onNavigateToDetail
                )
                ViewMode.TABLE -> FdTableView(
                    fds = uiState.filteredFds,
                    onFdClick = onNavigateToDetail
                )
                ViewMode.CARD -> FdCardView(
                    fds = uiState.filteredFds,
                    onFdClick = onNavigateToDetail
                )
                ViewMode.GRID -> FdGridView(
                    fds = uiState.filteredFds,
                    onFdClick = onNavigateToDetail
                )
            }
        }
    }
}
