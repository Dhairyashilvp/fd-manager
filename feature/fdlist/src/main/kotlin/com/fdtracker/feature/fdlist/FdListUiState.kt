package com.fdtracker.feature.fdlist

import com.fdtracker.core.domain.model.FixedDeposit

enum class SortBy { MATURITY_DATE, PRINCIPAL, INTEREST_RATE }
enum class ViewMode { LIST, TABLE, CARD, GRID }

data class FdListUiState(
    val isLoading: Boolean = true,
    val fds: List<FixedDeposit> = emptyList(),
    val filteredFds: List<FixedDeposit> = emptyList(),
    val sortBy: SortBy = SortBy.MATURITY_DATE,
    val viewMode: ViewMode = ViewMode.LIST,
    val selectedBank: String? = null,
    val bankNames: List<String> = emptyList(),
    val error: String? = null
)

sealed interface FdListEvent {
    data class SetSortBy(val sortBy: SortBy) : FdListEvent
    data class SetViewMode(val viewMode: ViewMode) : FdListEvent
    data class FilterByBank(val bankName: String?) : FdListEvent
    data class NavigateToDetail(val fdId: String) : FdListEvent
    data object NavigateToAddFd : FdListEvent
}
