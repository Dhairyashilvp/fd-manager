package com.fdtracker.feature.fdlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.usecase.fd.GetAllActiveFdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FdListViewModel @Inject constructor(
    private val getAllActiveFds: GetAllActiveFdsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FdListUiState())
    val uiState: StateFlow<FdListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllActiveFds()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { fds ->
                    val bankNames = fds.map { it.bankName }.distinct().sorted()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            fds = fds,
                            bankNames = bankNames,
                            filteredFds = applyFiltersAndSort(fds, state.sortBy, state.selectedBank)
                        )
                    }
                }
        }
    }

    fun onEvent(event: FdListEvent) {
        when (event) {
            is FdListEvent.SetSortBy -> {
                _uiState.update { state ->
                    state.copy(
                        sortBy = event.sortBy,
                        filteredFds = applyFiltersAndSort(state.fds, event.sortBy, state.selectedBank)
                    )
                }
            }
            is FdListEvent.SetViewMode -> {
                _uiState.update { it.copy(viewMode = event.viewMode) }
            }
            is FdListEvent.FilterByBank -> {
                _uiState.update { state ->
                    state.copy(
                        selectedBank = event.bankName,
                        filteredFds = applyFiltersAndSort(state.fds, state.sortBy, event.bankName)
                    )
                }
            }
            else -> {}
        }
    }

    private fun applyFiltersAndSort(
        fds: List<com.fdtracker.core.domain.model.FixedDeposit>,
        sortBy: SortBy,
        bankFilter: String?
    ): List<com.fdtracker.core.domain.model.FixedDeposit> {
        val filtered = if (bankFilter != null) fds.filter { it.bankName == bankFilter } else fds
        return when (sortBy) {
            SortBy.MATURITY_DATE -> filtered.sortedBy { it.maturityDate }
            SortBy.PRINCIPAL -> filtered.sortedByDescending { it.principalAmount }
            SortBy.INTEREST_RATE -> filtered.sortedByDescending { it.interestRatePA }
        }
    }
}
