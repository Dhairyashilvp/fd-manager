package com.fdtracker.feature.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.OcrParsedFd
import com.fdtracker.core.domain.usecase.ocr.ParseOcrResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val isProcessing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val rawText: String = "",
    val parsedFd: OcrParsedFd? = null,
    val error: String? = null
)

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val parseOcrResult: ParseOcrResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun onTextRecognized(rawText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, rawText = rawText) }
            try {
                val parsed = parseOcrResult(rawText)
                _uiState.update { it.copy(isProcessing = false, parsedFd = parsed) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        _uiState.update { it.copy(capturedBitmap = bitmap) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reset() {
        _uiState.value = OcrUiState()
    }
}
