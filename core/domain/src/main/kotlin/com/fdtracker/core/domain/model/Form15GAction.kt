package com.fdtracker.core.domain.model

import java.math.BigDecimal

data class Form15GAction(
    val bankName: String,
    val formType: TaxExemptionForm,
    val projectedInterest: BigDecimal,
    val isRequired: Boolean,
    val isSubmitted: Boolean
)
