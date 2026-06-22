package com.fdtracker.core.domain.model

import java.time.LocalDate

data class UserProfile(
    val fullName: String,
    val dateOfBirth: LocalDate,
    val panNumber: String? = null,
    val isSeniorCitizen: Boolean = false
)
