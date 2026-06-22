package com.fdtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fdtracker.feature.calendar.CalendarScreen
import com.fdtracker.feature.dashboard.DashboardScreen
import com.fdtracker.feature.fddetail.EditFdScreen
import com.fdtracker.feature.fddetail.FdDetailScreen
import com.fdtracker.feature.fdlist.FdListScreen
import com.fdtracker.feature.ocr.OcrCaptureScreen
import com.fdtracker.feature.ocr.OcrReviewScreen
import com.fdtracker.feature.settings.SettingsScreen
import com.fdtracker.feature.strategy.StrategyScreen
import com.fdtracker.feature.tax.TaxScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToFd = { fdId ->
                    navController.navigate(FdDetailRoute(fdId))
                },
                onNavigateToOcr = {
                    navController.navigate(OcrCaptureRoute)
                },
                onNavigateToTax = {
                    navController.navigate(TaxRoute)
                },
                onNavigateToStrategy = {
                    navController.navigate(StrategyRoute)
                }
            )
        }

        composable<FdListRoute> {
            FdListScreen(
                onNavigateToDetail = { fdId ->
                    navController.navigate(FdDetailRoute(fdId))
                },
                onNavigateToAddFd = {
                    navController.navigate(EditFdRoute())
                }
            )
        }

        composable<FdDetailRoute> {
            val route = it.toRoute<FdDetailRoute>()
            FdDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { fdId ->
                    navController.navigate(EditFdRoute(fdId))
                }
            )
        }

        composable<EditFdRoute> {
            EditFdScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CalendarRoute> {
            CalendarScreen(
                onNavigateToFd = { fdId ->
                    navController.navigate(FdDetailRoute(fdId))
                }
            )
        }

        composable<OcrCaptureRoute> {
            OcrCaptureScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReview = {
                    navController.navigate(OcrReviewRoute)
                }
            )
        }

        composable<OcrReviewRoute> {
            OcrReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditFd = { bankName, fdNumber, principal, rate, valueDate, maturityDate, maturityAmount, holderName ->
                    navController.navigate(
                        EditFdRoute(
                            ocrBankName = bankName,
                            ocrFdNumber = fdNumber,
                            ocrPrincipal = principal,
                            ocrRate = rate,
                            ocrValueDate = valueDate,
                            ocrMaturityDate = maturityDate,
                            ocrMaturityAmount = maturityAmount,
                            ocrHolderName = holderName
                        )
                    ) {
                        popUpTo(OcrCaptureRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<TaxRoute> {
            TaxScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<StrategyRoute> {
            StrategyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}
