package com.fdtracker.core.data.di

import com.fdtracker.core.domain.repository.BankRepository
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.repository.ReminderRepository
import com.fdtracker.core.domain.usecase.calculation.CalculateAccruedInterestUseCase
import com.fdtracker.core.domain.usecase.calculation.CalculateBreakFdPenaltyUseCase
import com.fdtracker.core.domain.usecase.calculation.CalculateMaturityUseCase
import com.fdtracker.core.domain.usecase.calendar.GetCashFlowForecastUseCase
import com.fdtracker.core.domain.usecase.calendar.GetMaturityTimelineUseCase
import com.fdtracker.core.domain.usecase.dashboard.CheckInsuranceLimitUseCase
import com.fdtracker.core.domain.usecase.dashboard.GetBankExposureUseCase
import com.fdtracker.core.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.fdtracker.core.domain.usecase.dashboard.GetWeightedYieldUseCase
import com.fdtracker.core.domain.usecase.fd.AddOrUpdateFdUseCase
import com.fdtracker.core.domain.usecase.fd.DeleteFdUseCase
import com.fdtracker.core.domain.usecase.fd.GetAllActiveFdsUseCase
import com.fdtracker.core.domain.usecase.fd.GetFdByIdUseCase
import com.fdtracker.core.domain.usecase.ocr.ParseOcrResultUseCase
import com.fdtracker.core.domain.usecase.reminder.ScheduleRemindersUseCase
import com.fdtracker.core.domain.usecase.strategy.AnalyzeLadderingUseCase
import com.fdtracker.core.domain.usecase.tax.GetForm15GChecklistUseCase
import com.fdtracker.core.domain.usecase.tax.GetTdsThresholdStatusUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetAllActiveFdsUseCase(repo: FdRepository) = GetAllActiveFdsUseCase(repo)

    @Provides
    fun provideGetFdByIdUseCase(repo: FdRepository) = GetFdByIdUseCase(repo)

    @Provides
    fun provideAddOrUpdateFdUseCase(repo: FdRepository, bankRepo: BankRepository) =
        AddOrUpdateFdUseCase(repo, bankRepo)

    @Provides
    fun provideDeleteFdUseCase(repo: FdRepository) = DeleteFdUseCase(repo)

    @Provides
    fun provideCalculateMaturityUseCase() = CalculateMaturityUseCase()

    @Provides
    fun provideCalculateAccruedInterestUseCase() = CalculateAccruedInterestUseCase()

    @Provides
    fun provideCalculateBreakFdPenaltyUseCase(repo: FdRepository) =
        CalculateBreakFdPenaltyUseCase(repo)

    @Provides
    fun provideGetDashboardSummaryUseCase(
        repo: FdRepository,
        accruedUseCase: CalculateAccruedInterestUseCase
    ) = GetDashboardSummaryUseCase(repo, accruedUseCase)

    @Provides
    fun provideGetBankExposureUseCase(repo: FdRepository, bankRepo: BankRepository) =
        GetBankExposureUseCase(repo, bankRepo)

    @Provides
    fun provideGetWeightedYieldUseCase(repo: FdRepository) = GetWeightedYieldUseCase(repo)

    @Provides
    fun provideCheckInsuranceLimitUseCase(getBankExposure: GetBankExposureUseCase) =
        CheckInsuranceLimitUseCase(getBankExposure)

    @Provides
    fun provideGetMaturityTimelineUseCase(repo: FdRepository) = GetMaturityTimelineUseCase(repo)

    @Provides
    fun provideGetCashFlowForecastUseCase(repo: FdRepository) = GetCashFlowForecastUseCase(repo)

    @Provides
    fun provideGetTdsThresholdStatusUseCase(repo: FdRepository) =
        GetTdsThresholdStatusUseCase(repo)

    @Provides
    fun provideGetForm15GChecklistUseCase(repo: FdRepository) = GetForm15GChecklistUseCase(repo)

    @Provides
    fun provideAnalyzeLadderingUseCase(repo: FdRepository) = AnalyzeLadderingUseCase(repo)

    @Provides
    fun provideParseOcrResultUseCase() = ParseOcrResultUseCase()

    @Provides
    fun provideScheduleRemindersUseCase(repo: ReminderRepository) =
        ScheduleRemindersUseCase(repo)
}
