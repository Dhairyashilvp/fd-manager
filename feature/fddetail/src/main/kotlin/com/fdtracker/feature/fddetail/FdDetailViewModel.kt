package com.fdtracker.feature.fddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.usecase.calculation.CalculateAccruedInterestUseCase
import com.fdtracker.core.domain.usecase.fd.DeleteFdUseCase
import com.fdtracker.core.domain.usecase.fd.GetFdByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class FdDetailUiState(
    val isLoading: Boolean = true,
    val fd: FixedDeposit? = null,
    val accruedInterest: BigDecimal = BigDecimal.ZERO,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FdDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFdById: GetFdByIdUseCase,
    private val deleteFd: DeleteFdUseCase,
    private val calculateAccruedInterest: CalculateAccruedInterestUseCase
) : ViewModel() {

    private val fdId: String = savedStateHandle.get<String>("fdId") ?: ""

    private val _uiState = MutableStateFlow(FdDetailUiState())
    val uiState: StateFlow<FdDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getFdById(fdId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { fd ->
                    val accrued = fd?.let { calculateAccruedInterest(it, LocalDate.now()) } ?: BigDecimal.ZERO
                    _uiState.update {
                        it.copy(isLoading = false, fd = fd, accruedInterest = accrued)
                    }
                }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteFd() {
        viewModelScope.launch {
            val result = deleteFd(fdId)
            if (result.isSuccess) {
                _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
            } else {
                _uiState.update { it.copy(showDeleteDialog = false, error = "Failed to delete FD") }
            }
        }
    }
}
