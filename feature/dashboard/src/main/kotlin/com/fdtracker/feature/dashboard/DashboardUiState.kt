package com.fdtracker.feature.dashboard

import com.fdtracker.core.domain.model.BankExposure
import com.fdtracker.core.domain.model.FixedDeposit
import java.math.BigDecimal

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalPrincipal: BigDecimal = BigDecimal.ZERO,
    val totalAccrued: BigDecimal = BigDecimal.ZERO,
    val totalMaturityValue: BigDecimal = BigDecimal.ZERO,
    val weightedYield: BigDecimal = BigDecimal.ZERO,
    val activeFdCount: Int = 0,
    val bankExposures: List<BankExposure> = emptyList(),
    val overLimitBanks: List<BankExposure> = emptyList(),
    val upcomingMaturities: List<FixedDeposit> = emptyList(),
    val error: String? = null
)

sealed interface DashboardEvent {
    data object Refresh : DashboardEvent
    data class NavigateToFd(val fdId: String) : DashboardEvent
    data object NavigateToOcr : DashboardEvent
    data object NavigateToTax : DashboardEvent
    data object NavigateToStrategy : DashboardEvent
}
