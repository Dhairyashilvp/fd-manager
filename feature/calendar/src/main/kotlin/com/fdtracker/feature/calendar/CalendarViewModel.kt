package com.fdtracker.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.model.MonthlyCashFlow
import com.fdtracker.core.domain.usecase.calendar.GetCashFlowForecastUseCase
import com.fdtracker.core.domain.usecase.calendar.GetMaturityTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val maturityDates: Map<LocalDate, List<FixedDeposit>> = emptyMap(),
    val cashFlowForecast: List<MonthlyCashFlow> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedDateFds: List<FixedDeposit> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMaturityTimeline: GetMaturityTimelineUseCase,
    private val getCashFlowForecast: GetCashFlowForecastUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1)
            val endDate = LocalDate.now().plusMonths(24)

            getMaturityTimeline(startDate, endDate)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { fds ->
                    val grouped = fds.groupBy { it.maturityDate }
                    _uiState.update { it.copy(maturityDates = grouped) }
                }
        }

        viewModelScope.launch {
            getCashFlowForecast(24)
                .catch { }
                .collect { forecast ->
                    _uiState.update { it.copy(isLoading = false, cashFlowForecast = forecast) }
                }
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth) }
    }

    fun onDateSelected(date: LocalDate) {
        val fds = _uiState.value.maturityDates[date] ?: emptyList()
        _uiState.update { it.copy(selectedDate = date, selectedDateFds = fds) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedDate = null, selectedDateFds = emptyList()) }
    }
}
