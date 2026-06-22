package com.fdtracker.feature.fddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fdtracker.core.domain.model.FixedDeposit
import com.fdtracker.core.domain.model.HoldingMode
import com.fdtracker.core.domain.model.PayoutFrequency
import com.fdtracker.core.domain.model.RenewalInstruction
import com.fdtracker.core.domain.model.SpecialCategory
import com.fdtracker.core.domain.model.TaxExemptionForm
import com.fdtracker.core.domain.usecase.calculation.CalculateMaturityUseCase
import com.fdtracker.core.domain.usecase.fd.AddOrUpdateFdUseCase
import com.fdtracker.core.domain.usecase.fd.GetFdByIdUseCase
import com.fdtracker.core.domain.usecase.reminder.ScheduleRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class EditFdUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val fdAccountNumber: String = "",
    val bankName: String = "",
    val cifCustomerId: String = "",
    val primaryHolderName: String = "",
    val holdingMode: HoldingMode = HoldingMode.SINGLE,
    val jointHolderNames: String = "",
    val principalAmount: String = "",
    val valueDate: LocalDate = LocalDate.now(),
    val maturityDate: LocalDate = LocalDate.now().plusYears(1),
    val tenureDays: String = "365",
    val interestRatePA: String = "",
    val compoundingFrequency: PayoutFrequency = PayoutFrequency.QUARTERLY,
    val estimatedMaturityAmount: String = "",
    val autoRenewalInstruction: RenewalInstruction = RenewalInstruction.PAYOUT,
    val payoutAccountId: String = "",
    val gracePeriodDays: String = "7",
    val nomineeName: String = "",
    val interestPayoutFrequency: PayoutFrequency? = null,
    val taxTdsApplicable: Boolean = false,
    val taxExemptionForm: TaxExemptionForm = TaxExemptionForm.NONE,
    val specialCategory: SpecialCategory = SpecialCategory.STANDARD,
    val isTaxSaver: Boolean = false,
    val branchCode: String = "",
    val ifscCode: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditFdViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFdById: GetFdByIdUseCase,
    private val addOrUpdateFd: AddOrUpdateFdUseCase,
    private val calculateMaturity: CalculateMaturityUseCase,
    private val scheduleReminders: ScheduleRemindersUseCase
) : ViewModel() {

    private val fdId: String? = savedStateHandle.get<String>("fdId")
    private val ocrBankName: String? = savedStateHandle.get<String>("ocrBankName")
    private val ocrFdNumber: String? = savedStateHandle.get<String>("ocrFdNumber")
    private val ocrPrincipal: String? = savedStateHandle.get<String>("ocrPrincipal")
    private val ocrRate: String? = savedStateHandle.get<String>("ocrRate")
    private val ocrValueDate: String? = savedStateHandle.get<String>("ocrValueDate")
    private val ocrMaturityDate: String? = savedStateHandle.get<String>("ocrMaturityDate")
    private val ocrMaturityAmount: String? = savedStateHandle.get<String>("ocrMaturityAmount")
    private val ocrHolderName: String? = savedStateHandle.get<String>("ocrHolderName")

    private val _uiState = MutableStateFlow(EditFdUiState())
    val uiState: StateFlow<EditFdUiState> = _uiState.asStateFlow()

    init {
        if (fdId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val fd = getFdById(fdId).firstOrNull()
                if (fd != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            fdAccountNumber = fd.fdAccountNumber,
                            bankName = fd.bankName,
                            cifCustomerId = fd.cifCustomerId,
                            primaryHolderName = fd.primaryHolderName,
                            holdingMode = fd.holdingMode,
                            jointHolderNames = fd.jointHolderNames.joinToString(", "),
                            principalAmount = fd.principalAmount.toPlainString(),
                            valueDate = fd.valueDate,
                            maturityDate = fd.maturityDate,
                            tenureDays = fd.tenureDays.toString(),
                            interestRatePA = fd.interestRatePA.toPlainString(),
                            compoundingFrequency = fd.compoundingFrequency,
                            estimatedMaturityAmount = fd.estimatedMaturityAmount.toPlainString(),
                            autoRenewalInstruction = fd.autoRenewalInstruction,
                            payoutAccountId = fd.payoutAccountId,
                            gracePeriodDays = fd.gracePeriodDays.toString(),
                            nomineeName = fd.nomineeName ?: "",
                            interestPayoutFrequency = fd.interestPayoutFrequency,
                            taxTdsApplicable = fd.taxTdsApplicable ?: false,
                            taxExemptionForm = fd.taxExemptionForm,
                            specialCategory = fd.specialCategory,
                            isTaxSaver = fd.isTaxSaver,
                            branchCode = fd.branchCode ?: "",
                            ifscCode = fd.ifscCode ?: ""
                        )
                    }
                }
            }
        } else {
            applyOcrPrefill()
        }
    }

    private fun applyOcrPrefill() {
        val parsedValueDate = parseOcrDate(ocrValueDate)
        val parsedMaturityDate = parseOcrDate(ocrMaturityDate)
        val calculatedTenureDays = if (parsedValueDate != null && parsedMaturityDate != null && !parsedMaturityDate.isBefore(parsedValueDate)) {
            ChronoUnit.DAYS.between(parsedValueDate, parsedMaturityDate).toInt().toString()
        } else {
            null
        }

        _uiState.update { state ->
            state.copy(
                fdAccountNumber = sanitizeNumericText(ocrFdNumber),
                bankName = sanitizeText(ocrBankName),
                primaryHolderName = sanitizeText(ocrHolderName),
                principalAmount = sanitizeNumericText(ocrPrincipal),
                interestRatePA = sanitizeNumericText(ocrRate),
                valueDate = parsedValueDate ?: state.valueDate,
                maturityDate = parsedMaturityDate ?: state.maturityDate,
                tenureDays = calculatedTenureDays ?: state.tenureDays,
                estimatedMaturityAmount = sanitizeNumericText(ocrMaturityAmount)
            )
        }

        autoCalculate()
    }

    private fun sanitizeText(value: String?): String {
        return value?.trim().orEmpty()
    }

    private fun sanitizeNumericText(value: String?): String {
        return value
            ?.replace(",", "")
            ?.replace("₹", "")
            ?.replace("%", "")
            ?.trim()
            .orEmpty()
    }

    private fun parseOcrDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null

        val cleaned = value.trim()
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("d.M.uuuu"),
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("d-M-yy"),
            DateTimeFormatter.ofPattern("d.M.yy")
        )

        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(cleaned, formatter) }.getOrNull()
        }
    }

    fun updateField(field: String, value: Any) {
        _uiState.update { state ->
            when (field) {
                "fdAccountNumber" -> state.copy(fdAccountNumber = value as String)
                "bankName" -> state.copy(bankName = value as String)
                "cifCustomerId" -> state.copy(cifCustomerId = value as String)
                "primaryHolderName" -> state.copy(primaryHolderName = value as String)
                "holdingMode" -> state.copy(holdingMode = value as HoldingMode)
                "jointHolderNames" -> state.copy(jointHolderNames = value as String)
                "principalAmount" -> state.copy(principalAmount = value as String)
                "interestRatePA" -> state.copy(interestRatePA = value as String)
                "tenureDays" -> state.copy(tenureDays = value as String)
                "compoundingFrequency" -> state.copy(compoundingFrequency = value as PayoutFrequency)
                "autoRenewalInstruction" -> state.copy(autoRenewalInstruction = value as RenewalInstruction)
                "payoutAccountId" -> state.copy(payoutAccountId = value as String)
                "gracePeriodDays" -> state.copy(gracePeriodDays = value as String)
                "nomineeName" -> state.copy(nomineeName = value as String)
                "interestPayoutFrequency" -> state.copy(interestPayoutFrequency = value as? PayoutFrequency)
                "taxTdsApplicable" -> state.copy(taxTdsApplicable = value as Boolean)
                "taxExemptionForm" -> state.copy(taxExemptionForm = value as TaxExemptionForm)
                "specialCategory" -> state.copy(specialCategory = value as SpecialCategory)
                "isTaxSaver" -> state.copy(isTaxSaver = value as Boolean)
                "branchCode" -> state.copy(branchCode = value as String)
                "ifscCode" -> state.copy(ifscCode = value as String)
                else -> state
            }
        }
        // Auto-calculate maturity when relevant fields change
        if (field in listOf("principalAmount", "interestRatePA", "tenureDays", "compoundingFrequency")) {
            autoCalculate()
        }
    }

    fun updateValueDate(date: LocalDate) {
        _uiState.update { it.copy(valueDate = date) }
        autoCalculate()
    }

    private fun autoCalculate() {
        val state = _uiState.value
        try {
            val principal = BigDecimal(state.principalAmount.ifBlank { "0" })
            val rate = BigDecimal(state.interestRatePA.ifBlank { "0" })
            val tenure = state.tenureDays.toIntOrNull() ?: 0

            if (principal > BigDecimal.ZERO && rate > BigDecimal.ZERO && tenure > 0) {
                val result = calculateMaturity(principal, rate, tenure, state.compoundingFrequency, state.valueDate)
                _uiState.update {
                    it.copy(
                        maturityDate = result.maturityDate,
                        estimatedMaturityAmount = result.maturityAmount.toPlainString()
                    )
                }
            }
        } catch (_: Exception) { }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val fd = FixedDeposit(
                    fdAccountNumber = state.fdAccountNumber,
                    bankName = state.bankName,
                    cifCustomerId = state.cifCustomerId,
                    primaryHolderName = state.primaryHolderName,
                    holdingMode = state.holdingMode,
                    jointHolderNames = state.jointHolderNames.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    principalAmount = BigDecimal(state.principalAmount),
                    valueDate = state.valueDate,
                    maturityDate = state.maturityDate,
                    tenureDays = state.tenureDays.toInt(),
                    interestRatePA = BigDecimal(state.interestRatePA),
                    compoundingFrequency = state.compoundingFrequency,
                    estimatedMaturityAmount = BigDecimal(state.estimatedMaturityAmount.ifBlank { "0" }),
                    autoRenewalInstruction = state.autoRenewalInstruction,
                    payoutAccountId = state.payoutAccountId,
                    gracePeriodDays = state.gracePeriodDays.toIntOrNull() ?: 7,
                    nomineeName = state.nomineeName.ifBlank { null },
                    interestPayoutFrequency = state.interestPayoutFrequency,
                    taxTdsApplicable = state.taxTdsApplicable,
                    taxExemptionForm = state.taxExemptionForm,
                    specialCategory = state.specialCategory,
                    isTaxSaver = state.isTaxSaver,
                    branchCode = state.branchCode.ifBlank { null },
                    ifscCode = state.ifscCode.ifBlank { null }
                )

                val result = addOrUpdateFd(fd)
                if (result.isSuccess) {
                    scheduleReminders(fd)
                    _uiState.update { it.copy(isSaved = true) }
                } else {
                    _uiState.update { it.copy(error = "Failed to save FD") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Invalid input") }
            }
        }
    }
}
