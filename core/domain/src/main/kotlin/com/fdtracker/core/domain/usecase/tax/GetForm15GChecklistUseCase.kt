package com.fdtracker.core.domain.usecase.tax

import com.fdtracker.core.common.Constants
import com.fdtracker.core.common.financialYearEnd
import com.fdtracker.core.common.financialYearStart
import com.fdtracker.core.domain.model.Form15GAction
import com.fdtracker.core.domain.model.TaxExemptionForm
import com.fdtracker.core.domain.model.UserProfile
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.util.InterestCalculator
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Period

class GetForm15GChecklistUseCase(
    private val fdRepository: FdRepository
) {
    suspend operator fun invoke(userProfile: UserProfile): List<Form15GAction> {
        val today = LocalDate.now()
        val fyStart = today.financialYearStart()
        val fyEnd = today.financialYearEnd()
        val age = Period.between(userProfile.dateOfBirth, today).years
        val isSenior = age >= Constants.SENIOR_CITIZEN_AGE
        val formType = if (isSenior) TaxExemptionForm.FORM_15H else TaxExemptionForm.FORM_15G
        val threshold = if (isSenior) Constants.TDS_THRESHOLD_SENIOR_CITIZEN
                       else Constants.TDS_THRESHOLD_GENERAL

        val fds = fdRepository.observeAllActive().first()

        return fds.groupBy { it.bankName }.map { (bankName, bankFds) ->
            val projectedInterest = bankFds.fold(BigDecimal.ZERO) { acc, fd ->
                val fdStart = maxOf(fd.valueDate, fyStart)
                val fdEnd = minOf(fd.maturityDate, fyEnd)
                if (fdStart.isAfter(fdEnd)) return@fold acc

                val interest = InterestCalculator.calculateAccruedInterest(
                    principal = fd.principalAmount,
                    annualRate = fd.interestRatePA,
                    valueDate = fd.valueDate,
                    currentDate = fdEnd,
                    compounding = fd.compoundingFrequency
                )
                val priorInterest = if (fd.valueDate.isBefore(fyStart)) {
                    InterestCalculator.calculateAccruedInterest(
                        principal = fd.principalAmount,
                        annualRate = fd.interestRatePA,
                        valueDate = fd.valueDate,
                        currentDate = fyStart,
                        compounding = fd.compoundingFrequency
                    )
                } else BigDecimal.ZERO
                acc.add(interest.subtract(priorInterest))
            }

            val isRequired = projectedInterest >= threshold
            val isSubmitted = bankFds.any { it.taxExemptionForm == formType }

            Form15GAction(
                bankName = bankName,
                formType = formType,
                projectedInterest = projectedInterest,
                isRequired = isRequired,
                isSubmitted = isSubmitted
            )
        }.sortedByDescending { it.projectedInterest }
    }
}
