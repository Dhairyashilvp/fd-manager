package com.fdtracker.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fdtracker.core.common.Constants
import com.fdtracker.core.domain.model.UserProfile
import com.fdtracker.core.domain.repository.UserPrefsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Singleton
class UserPrefsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPrefsRepository {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_DOB = longPreferencesKey("user_dob")
        val USER_PAN = stringPreferencesKey("user_pan")
        val EMAIL_REMINDERS = booleanPreferencesKey("email_reminders")
        val PUSH_REMINDERS = booleanPreferencesKey("push_reminders")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val DARK_MODE = stringPreferencesKey("dark_mode")
    }

    override fun observeUserProfile(): Flow<UserProfile?> {
        return context.dataStore.data.map { prefs ->
            val name = prefs[Keys.USER_NAME] ?: return@map null
            val dobMillis = prefs[Keys.USER_DOB] ?: return@map null
            val dob = Instant.ofEpochMilli(dobMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val age = java.time.Period.between(dob, LocalDate.now()).years
            UserProfile(
                fullName = name,
                dateOfBirth = dob,
                panNumber = prefs[Keys.USER_PAN],
                isSeniorCitizen = age >= Constants.SENIOR_CITIZEN_AGE
            )
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = profile.fullName
            prefs[Keys.USER_DOB] = profile.dateOfBirth
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            profile.panNumber?.let { prefs[Keys.USER_PAN] = it }
        }
    }

    override fun observeEmailRemindersEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.EMAIL_REMINDERS] ?: false }
    }

    override suspend fun setEmailRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EMAIL_REMINDERS] = enabled }
    }

    override fun observePushRemindersEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.PUSH_REMINDERS] ?: true }
    }

    override suspend fun setPushRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PUSH_REMINDERS] = enabled }
    }

    override fun observeAppLockEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[Keys.APP_LOCK] ?: false }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK] = enabled }
    }

    override fun observeDarkMode(): Flow<Boolean?> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.DARK_MODE]?.toBooleanStrictOrNull()
        }
    }

    override suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { prefs ->
            if (enabled != null) {
                prefs[Keys.DARK_MODE] = enabled.toString()
            } else {
                prefs.remove(Keys.DARK_MODE)
            }
        }
    }
}
