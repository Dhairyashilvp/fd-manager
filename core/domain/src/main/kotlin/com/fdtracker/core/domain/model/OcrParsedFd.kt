package com.fdtracker.core.domain.model

data class OcrParsedFd(
    val bankName: String? = null,
    val fdAccountNumber: String? = null,
    val principalAmount: String? = null,
    val interestRate: String? = null,
    val valueDate: String? = null,
    val maturityDate: String? = null,
    val tenure: String? = null,
    val maturityAmount: String? = null,
    val holderName: String? = null,
    val confidence: Float = 0f
)
