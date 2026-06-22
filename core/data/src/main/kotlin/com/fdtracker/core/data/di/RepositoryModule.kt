package com.fdtracker.core.data.di

import com.fdtracker.core.data.repository.BankRepositoryImpl
import com.fdtracker.core.data.repository.FdRepositoryImpl
import com.fdtracker.core.data.repository.ReminderRepositoryImpl
import com.fdtracker.core.data.repository.SmtpRepositoryImpl
import com.fdtracker.core.data.repository.UserPrefsRepositoryImpl
import com.fdtracker.core.domain.repository.BankRepository
import com.fdtracker.core.domain.repository.FdRepository
import com.fdtracker.core.domain.repository.ReminderRepository
import com.fdtracker.core.domain.repository.SmtpRepository
import com.fdtracker.core.domain.repository.UserPrefsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFdRepository(impl: FdRepositoryImpl): FdRepository

    @Binds
    @Singleton
    abstract fun bindBankRepository(impl: BankRepositoryImpl): BankRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindSmtpRepository(impl: SmtpRepositoryImpl): SmtpRepository

    @Binds
    @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPrefsRepositoryImpl): UserPrefsRepository
}
