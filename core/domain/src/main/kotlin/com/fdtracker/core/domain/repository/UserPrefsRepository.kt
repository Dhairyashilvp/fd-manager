package com.fdtracker.core.domain.repository

import com.fdtracker.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserPrefsRepository {
    fun observeUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
    fun observeEmailRemindersEnabled(): Flow<Boolean>
    suspend fun setEmailRemindersEnabled(enabled: Boolean)
    fun observePushRemindersEnabled(): Flow<Boolean>
    suspend fun setPushRemindersEnabled(enabled: Boolean)
    fun observeAppLockEnabled(): Flow<Boolean>
    suspend fun setAppLockEnabled(enabled: Boolean)
    fun observeDarkMode(): Flow<Boolean?>
    suspend fun setDarkMode(enabled: Boolean?)
}
