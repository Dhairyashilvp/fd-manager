package com.fdtracker.navigation

import kotlinx.serialization.Serializable

@Serializable
object DashboardRoute

@Serializable
object FdListRoute

@Serializable
data class FdDetailRoute(val fdId: String)

@Serializable
data class EditFdRoute(
    val fdId: String? = null,
    val ocrBankName: String? = null,
    val ocrFdNumber: String? = null,
    val ocrPrincipal: String? = null,
    val ocrRate: String? = null,
    val ocrValueDate: String? = null,
    val ocrMaturityDate: String? = null,
    val ocrMaturityAmount: String? = null,
    val ocrHolderName: String? = null
)

@Serializable
object CalendarRoute

@Serializable
object OcrCaptureRoute

@Serializable
object OcrReviewRoute

@Serializable
object TaxRoute

@Serializable
object StrategyRoute

@Serializable
object SettingsRoute
