package com.fdtracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.usecase.dashboard.CheckInsuranceLimitUseCase
import com.fdtracker.core.domain.usecase.dashboard.GetBankExposureUseCase
import com.fdtracker.core.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.fdtracker.core.domain.usecase.fd.GetAllActiveFdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardSummary: GetDashboardSummaryUseCase,
    getBankExposure: GetBankExposureUseCase,
    checkInsuranceLimit: CheckInsuranceLimitUseCase,
    getAllActiveFds: GetAllActiveFdsUseCase
) : ViewModel() {

    private val _navigateEvent = MutableStateFlow<DashboardEvent?>(null)
    val navigateEvent: StateFlow<DashboardEvent?> = _navigateEvent

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardSummary(),
        getBankExposure(),
        checkInsuranceLimit(),
        getAllActiveFds()
    ) { summary, exposures, overLimit, allFds ->
        val today = LocalDate.now()
        val upcoming = allFds
            .filter { it.maturityDate.isAfter(today) && it.maturityDate.isBefore(today.plusDays(31)) }
            .sortedBy { it.maturityDate }
            .take(5)

        DashboardUiState(
            isLoading = false,
            totalPrincipal = summary.totalPrincipal,
            totalAccrued = summary.totalAccruedInterest,
            totalMaturityValue = summary.totalMaturityValue,
            weightedYield = summary.weightedAverageYield,
            activeFdCount = summary.activeFdCount,
            bankExposures = exposures,
            overLimitBanks = overLimit,
            upcomingMaturities = upcoming,
            error = null
        )
    }.catch { e ->
        emit(DashboardUiState(isLoading = false, error = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onEvent(event: DashboardEvent) {
        _navigateEvent.value = event
    }

    fun consumeNavigationEvent() {
        _navigateEvent.value = null
    }
}
