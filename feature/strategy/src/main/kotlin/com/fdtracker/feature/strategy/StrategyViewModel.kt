package com.fdtracker.feature.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.BreakFdRecommendation
import com.fdtracker.core.domain.model.LadderAnalysis
import com.fdtracker.core.domain.usecase.calculation.CalculateBreakFdPenaltyUseCase
import com.fdtracker.core.domain.usecase.strategy.AnalyzeLadderingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class StrategyUiState(
    val isLoading: Boolean = true,
    val ladderAnalysis: LadderAnalysis? = null,
    val breakRecommendations: List<BreakFdRecommendation> = emptyList(),
    val targetAmount: String = "",
    val error: String? = null
)

@HiltViewModel
class StrategyViewModel @Inject constructor(
    private val analyzeLaddering: AnalyzeLadderingUseCase,
    private val calculateBreakFdPenalty: CalculateBreakFdPenaltyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrategyUiState())
    val uiState: StateFlow<StrategyUiState> = _uiState.asStateFlow()

    init {
        loadLadderAnalysis()
    }

    private fun loadLadderAnalysis() {
        viewModelScope.launch {
            try {
                val analysis = analyzeLaddering()
                _uiState.update { it.copy(isLoading = false, ladderAnalysis = analysis) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onTargetAmountChanged(amount: String) {
        _uiState.update { it.copy(targetAmount = amount) }
    }

    fun calculateBreakRecommendations() {
        viewModelScope.launch {
            try {
                val amount = BigDecimal(_uiState.value.targetAmount.ifBlank { "0" })
                if (amount <= BigDecimal.ZERO) return@launch
                val recommendations = calculateBreakFdPenalty(amount)
                _uiState.update { it.copy(breakRecommendations = recommendations) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
