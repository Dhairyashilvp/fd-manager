package com.fdtracker.core.data.di

import com.fdtracker.core.data.smtp.SmtpEmailService
import com.fdtracker.core.domain.repository.SmtpRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmtpModule {

    @Provides
    @Singleton
    fun provideSmtpEmailService(smtpRepository: SmtpRepository): SmtpEmailService {
        return SmtpEmailService(smtpRepository)
    }
}
