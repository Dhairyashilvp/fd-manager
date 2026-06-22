package com.fdtracker.feature.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.BankTdsStatus
import com.fdtracker.core.domain.model.Form15GAction
import com.fdtracker.core.domain.model.UserProfile
import com.fdtracker.core.domain.repository.UserPrefsRepository
import com.fdtracker.core.domain.usecase.tax.GetForm15GChecklistUseCase
import com.fdtracker.core.domain.usecase.tax.GetTdsThresholdStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaxUiState(
    val isLoading: Boolean = true,
    val tdsStatus: List<BankTdsStatus> = emptyList(),
    val form15GChecklist: List<Form15GAction> = emptyList(),
    val isSeniorCitizen: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null
)

@HiltViewModel
class TaxViewModel @Inject constructor(
    private val getTdsThresholdStatus: GetTdsThresholdStatusUseCase,
    private val getForm15GChecklist: GetForm15GChecklistUseCase,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaxUiState())
    val uiState: StateFlow<TaxUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = userPrefsRepository.observeUserProfile().first()
            val isSenior = profile?.isSeniorCitizen ?: false

            getTdsThresholdStatus(isSenior)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { tdsStatus ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tdsStatus = tdsStatus,
                            isSeniorCitizen = isSenior,
                            userProfile = profile
                        )
                    }
                }
        }

        viewModelScope.launch {
            val profile = userPrefsRepository.observeUserProfile().first()
            if (profile != null) {
                try {
                    val checklist = getForm15GChecklist(profile)
                    _uiState.update { it.copy(form15GChecklist = checklist) }
                } catch (_: Exception) { }
            }
        }
    }
}
